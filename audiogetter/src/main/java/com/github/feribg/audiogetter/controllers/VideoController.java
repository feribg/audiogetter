package com.github.feribg.audiogetter.controllers;

import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.TrackBox;
import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.models.Chunk;
import com.github.feribg.audiogetter.models.Chunks;
import com.google.inject.Singleton;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * A controller to interact with YouTube
 */
@Singleton
public class VideoController {


    private static byte[] mergeArrays(byte[] arr1, byte[] arr2) {
        return ArrayUtils.addAll(arr1, arr2);
    }

    public static byte[] addADTStoPacket(int packetLen) {
        byte[] packet = new byte[7];
        packetLen += 7; //packet length must count the ADTS header itself
        int profile = 2;  //AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

        return packet;
    }

    public byte[] adjustSample(byte[] originalSample) {
        byte[] header = addADTStoPacket(originalSample.length);
        return mergeArrays(header, originalSample);
    }


    /**
     * Given a moov atom array of bytes parse it and return an array list of Chunks to be downloaded later
     *
     * @param videoUrl valid mp4 url
     * @return list of Chunk objects ready to be processed with range requests
     * @throws Exception
     */
    public Chunks getChunks(String videoUrl) throws Exception {
        long moovAtomSize = fetchAtomSize(videoUrl);

        byte[] moovAtomBytes = Ion.with(App.ctx)
                .load(videoUrl)
                .addHeader("Accept-Ranges", "bytes")
                .addHeader("Range", "bytes=0-" + String.valueOf(moovAtomSize))
                .asByteArray().get();

        IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(moovAtomBytes));
        //get the moov header atom as defined in the mpeg-4 standard
        MovieBox moov = isoFile.getMovieBox();
        //since youtube mp4s have the audio track as the second track always, get only the audio header info
        TrackBox audiotrack = (TrackBox) moov.getBoxes().get(3);
        //retrieve samples atom information for the audio track
        SampleTableBox samples = audiotrack.getSampleTableBox();
        //info about the size in bytes for each sample
        SampleSizeBox sampleSizeBox = samples.getSampleSizeBox();
        //contains offsets of each chunk from the beggining of the file
        ChunkOffsetBox chunkOffsetBox = samples.getChunkOffsetBox();
        long[] chunkOffsets = chunkOffsetBox.getChunkOffsets();
        //contains infromation which sample belongs to which chunk
        SampleToChunkBox sampleToChunkBox = samples.getSampleToChunkBox();
        long[] samplesPerChunk = sampleToChunkBox.blowup(chunkOffsets.length);
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        //keeps track of current number of samples processed
        int sampleCount = 0;
        //this variable adjusts the chunk offset to reset the 1st one to 0 and go from there
        Long offsetDifference = chunkOffsets[0];
        for (int i = 0; i < chunkOffsets.length; i++) {
            long currentChunkSize = 0;
            for (int y = 0; y < samplesPerChunk[i]; y++) {
                int sampleNum = sampleCount + y;
                currentChunkSize += sampleSizeBox.getSampleSizeAtIndex(sampleNum);
            }
            sampleCount += samplesPerChunk[i];
            long chunkStartOffset = chunkOffsets[i];
            long chunkEndOffset = chunkStartOffset + currentChunkSize - 1; //-1 to adjust for the offset including the first starting byte
            Long offsetInFile = chunkStartOffset - offsetDifference;
            //add a Chunk object containg its position in the file, start offset from beginning of file and end offset
            chunks.add(new Chunk(i, chunkStartOffset, chunkEndOffset, offsetInFile));
        }

        return new Chunks(chunks, sampleSizeBox);
    }

    /**
     * Given an mp4 url try to find the number of bytes for the moov atom from the begining of the file in order to download it
     *
     * @param url a valid mp4 file url
     * @return number of bytes to fetch in order to get complete moov atom
     * @throws Exception
     */
    public long fetchAtomSize(String url) throws Exception {
        URL remote = new URL(url);
        URLConnection urlConnection = remote.openConnection();
        InputStream input = urlConnection.getInputStream();
        //Open a buffer and begin parsing content until the moov atom is reached
        ReadableByteChannel inChannel = Channels.newChannel(input);
        long bytesCount = 0; //begining content offset
        while (true) {
            //if we cant find the moov atom in the first 128KB its probably missing
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            ByteBuffer bb = ByteBuffer.allocate(8);
            inChannel.read(bb);
            bb.flip();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);

            bb.clear();
            if (type.equals("moov")) {
                Log.d(App.TAG, "BytesCount:" + bytesCount);
                input.close();
                Log.d(App.TAG, String.format("Total bytes before moov atom: %d", bytesCount));
                return size + bytesCount;
            }
            bytesCount += 8; //we increment the bytes read by 8 on each iteration

        }
    }

}
