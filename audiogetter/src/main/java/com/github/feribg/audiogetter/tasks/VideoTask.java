package com.github.feribg.audiogetter.tasks;

import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.controllers.VideoController;
import com.github.feribg.audiogetter.events.ProgressEvent;
import com.github.feribg.audiogetter.events.StartEvent;
import com.github.feribg.audiogetter.exceptions.InvalidSourceException;
import com.github.feribg.audiogetter.helpers.Utils;
import com.github.feribg.audiogetter.models.Chunk;
import com.github.feribg.audiogetter.models.Chunks;
import com.github.feribg.audiogetter.models.Download;
import com.google.inject.Inject;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;

public abstract class VideoTask extends BaseTask {

    @Inject
    protected VideoController videoController;

    protected VideoTask(Integer taskID, Download download, int iconRes) {
        super(taskID, download, iconRes);
    }

    @Override
    public void run() {
        try {
            EventBus.getDefault().post(new StartEvent(taskID));

            //only run if thread hasn't been interrupted
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Long startTime = System.currentTimeMillis();

            //set reference to the thread that's running the task so it can be interrupted
            setCurrentThread(Thread.currentThread());
            mBuilder.setContentText("Download in progress");
            notificationManager.notify(taskID, mBuilder.build());
            Chunks chunksData = videoController.getChunks(download.getDownloadUrl());

            //download the individual audio chunks
            Map<Integer, File> chunkFileMap = download(chunksData);

            assembleFile(chunkFileMap);

            //add the final audio headers
            finalizeAudio(chunksData);

            //change the notification to the complete state and index the file
            complete();

            Long endTime = System.currentTimeMillis();
            Log.d(App.TAG, "Total execution time: " + (endTime - startTime) + "ms");

        } catch (InterruptedException ex) {
            Log.d(App.TAG, ex.toString());
            cancelled();
            cleanup(true);
        } catch (Exception ex) {
            Log.d(App.TAG, ex.toString());
            failed();
            cleanup(true);
        } finally {
            Ion.getDefault(App.ctx).cancelAll(dlGroup);
            cleanup(false);
        }
    }

    protected void cleanup(Boolean removeOutputFile) {
        if (removeOutputFile && download.getDst() != null) {
            FileUtils.deleteQuietly(download.getDst());
        }
        if (download.getTmpDst() != null) {
            FileUtils.deleteQuietly(download.getTmpDst());
        }
        if (download.getTmpFolder() != null) {
            FileUtils.deleteQuietly(download.getTmpFolder());
        }
        if (download.getTmpDst2() != null) {
            FileUtils.deleteQuietly(download.getTmpDst2());
        }
    }


    protected Map<Integer, File> download(Chunks chunksData) throws Exception {
        ArrayList<Chunk> chunks = chunksData.getChunks();
        Log.d(App.TAG, "Num Chunks:" + chunks.size());
        if (chunks.size() < 1) {
            throw new InvalidSourceException("Something went wrong :(");
        }

        Log.d(App.TAG, "Chunks size: " + chunks.size());

        final Map<Integer, File> chunkFileMap = new TreeMap<Integer, File>();
        Log.d(App.TAG, "Folder to write: " + download.getTmpFolder().getAbsolutePath());
        List<List> partitions = Utils.partition(chunks, 75);
        Integer total = partitions.size();
        Log.d(App.TAG, "Number of partitions: " + total);
        for (List partition : partitions) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            EventBus.getDefault().post(new ProgressEvent(taskID, (long) progress.get(), (long) total));
            mBuilder.setProgress(100, Utils.normalizePercent(progress.get(), total), false);
            notificationManager.notify(taskID, mBuilder.build());
            Log.d(App.TAG, "Processing partition size:" + partition.size());
            final CountDownLatch doneSignal = new CountDownLatch(partition.size());
            for (Object aPartition : partition) {
                final Chunk chunk = (Chunk) aPartition;
                Ion.with(App.ctx)
                        .load(download.getDownloadUrl())
                        .addHeader("Accept-Ranges", "bytes")
                        .addHeader("Range", "bytes=" + String.valueOf(chunk.start) + "-" + String.valueOf(chunk.end))
                        .group(dlGroup)
                        .write(new File(download.getTmpFolder().getAbsolutePath(), chunk.getNumber() + ".part"))
                        .setCallback(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File file) {
                                if (e == null && file != null) {
                                    chunkFileMap.put(chunk.getNumber(), file);
                                } else {
                                    Log.e(App.TAG, "Something fucked up while downloading the audio", e);
                                }
                                doneSignal.countDown();
                            }
                        });
            }
            doneSignal.await();
            progress.incrementAndGet();
        }
        //if the final file map is not the same size as the original chunks some of the requests failed
        if (chunkFileMap.size() != chunks.size()) {
            throw new Exception("Some of the requests have most likely failed");
        }
        return chunkFileMap;
    }

    protected void assembleFile(Map<Integer, File> chunks) throws Exception {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        mBuilder.setContentText("Finalizing the audio");
        mBuilder.setProgress(0, 0, true);
        notificationManager.notify(taskID, mBuilder.build());
        Log.d(App.TAG, "Map Size: " + chunks.size());
        Log.d(App.TAG, "Files: " + chunks.toString());
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(download.getTmpDst()));
        for (Object o : chunks.entrySet()) {
            if (Thread.interrupted()) {
                IOUtils.closeQuietly(output);
                throw new InterruptedException();
            }
            Map.Entry pair = (Map.Entry) o;
            File sourceFile = (File) pair.getValue();
            InputStream input = new BufferedInputStream(new FileInputStream(sourceFile));
            IOUtils.copy(input, output);
            IOUtils.closeQuietly(input);
        }
        IOUtils.closeQuietly(output);
    }

    protected void finalizeAudio(Chunks chunksData) throws Exception {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        SampleSizeBox sampleSizeBox = chunksData.getSampleSizeBox();
        FileInputStream aacFis = new FileInputStream(download.getTmpDst());
        FileOutputStream newAac = new FileOutputStream(download.getTmpDst2(), true);
        int counter = 0;
        for (long sampleSize : sampleSizeBox.getSampleSizes()) {
            if (Thread.interrupted()) {
                aacFis.close();
                newAac.close();
                throw new InterruptedException();
            }
            counter++;
            byte[] originalSample = new byte[(int) sampleSize];
            aacFis.read(originalSample);
            byte[] modifiedSample = videoController.adjustSample(originalSample);
            newAac.write(modifiedSample);
            Log.d(App.TAG, String.format("Processing sample #%d, Original size: %d, Modified size: %d",
                    counter, originalSample.length, modifiedSample.length));
        }
        aacFis.close();
        newAac.close();

        FileDataSourceImpl aacSourceFile = new FileDataSourceImpl(download.getTmpDst2());
        Movie movie = new Movie();
        Track track = new AACTrackImpl(aacSourceFile);
        movie.addTrack(track);
        Container out = new DefaultMp4Builder().build(movie);
        FileOutputStream output = new FileOutputStream(download.getDst());
        out.writeContainer(output.getChannel());
        output.close();
    }

}
