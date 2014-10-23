package com.github.feribg.audiogetter.helpers;

import android.os.Build;
import android.util.Log;

import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.config.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /*
     * calculateMD5(File file)
	 * -----------------------
	 *
	 * Copyright (C) 2012 The CyanogenMod Project
	 *
	 * * Licensed under the GNU GPLv2 license
	 *
	 * The text of the license can be found in the LICENSE_GPL file
	 * or at https://www.gnu.org/licenses/gpl-2.0.txt
	 */
    public static String calculateMD5(File file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(App.TAG, "Exception while getting Digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(App.TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            //throw new RuntimeException("Unable to process file for MD5", e);
            Log.e(App.TAG, "Unable to process file for MD5", e); //TODO check if actually avoid FC
            return "00000000000000000000000000000000"; // fictional bad MD5: needed without "throw new RuntimeException"
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(App.TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    /**
     * Copy a file with the option to overwrite existing files
     *
     * @param src
     * @param dst
     * @param overwrite
     * @throws IOException
     */
    public static void copyFile(File src, File dst, Boolean overwrite) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        if (!dst.exists() || (dst.exists() && overwrite == true)) {
            try {
                if (dst.exists()) {
                    Log.d(App.TAG, "copyFile: destination exists but overwriting");
                }
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null) inChannel.close();
                if (outChannel != null) outChannel.close();
            }
        } else {
            Log.e(App.TAG, "copyFile: destination already exists:" + dst.getAbsolutePath());
            throw new IOException("copyFile: destination already exists:" + dst.getAbsolutePath());
        }
    }

    /**
     * Return a string containing specific CPU information for the device
     *
     * @return
     */
    public static String getCpuInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    sb.append(aLine + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Returns the correct arm version for use with ffmpeg
     *
     * @return
     */
    public static int armCpuVersion() {
        String cpuAbi = Build.CPU_ABI;
        Log.d(App.TAG, "CPU_ABI: " + cpuAbi);
        if (cpuAbi.equals("armeabi-v7a")) {
            return 7;
        } else if (cpuAbi.equals("armeabi")) {
            return 5;
        } else {
            return 0;
        }
    }


    /**
     * Make byte size in a readable format
     *
     * @param bytes
     * @param decimal
     * @return
     */
    public static String makeSizeHumanReadable(long bytes, boolean decimal) {
        String hr = "-";
        int unit = decimal ? 1000 : 1024;
        if (bytes < unit) {
            hr = bytes + " B";
        } else {
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (decimal ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (decimal ? "" : "i");
            hr = String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
        hr = hr.replace("-1 B", "-").replace("0 B", "-");
        return hr;
    }

    /**
     * Return integer percentage amount
     *
     * @param complete
     * @param total
     * @return
     */
    public static int normalizePercent(long complete, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(complete * 100 / total);
    }

    /**
     * Clean the filename, stripping illegal chars
     *
     * @param filename any string to be used for filename
     * @return clean filesystem safe filename
     */
    public static String cleanFilename(String filename) {
        filename = filename.replaceAll("[^a-z\\(\\)A-Z0-9\\.\\-]", "_");
        return filename;
    }

    /**
     * Merge two byte arrays
     *
     * @param arr1 first array
     * @param arr2 second array
     * @return combined byte array
     */
    public static byte[] mergeArrays(byte[] arr1, byte[] arr2) {
        byte[] combined = new byte[arr1.length + arr2.length];

        System.arraycopy(arr1, 0, combined, 0, arr1.length);
        System.arraycopy(arr2, 0, combined, arr1.length, arr2.length);
        return combined;
    }

    /**
     * Given a packet length generate a valid ADTS header to wrap it into
     *
     * @param packetLen number of bytes for that packet
     * @return correct byte array for ADTS header
     */
    private static byte[] generateAdtsHeader(int packetLen) {
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

    /**
     * @param path    relative path for the resource including / prefix
     * @param qparams NameValuePair list of parameters
     * @return a correctly formatted and urlencoded string
     * @throws URISyntaxException
     */
    public static URI getUri(String scheme, String path, List<NameValuePair> qparams) throws URISyntaxException {
        return URIUtils.createURI(scheme, Constants.Backend.API_HOST, -1, path, URLEncodedUtils.format(qparams, "UTF-8"), null);
    }

    /**
     * Prepare soundcloud search URL
     * @param query
     * @param offset
     * @return
     * @throws URISyntaxException
     */
    public static URI getSoundCloudSearchURI(String query, Integer offset) throws URISyntaxException {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("client_id", Constants.Soundcloud.CLIENT_ID));
        qparams.add(new BasicNameValuePair("offset", String.valueOf(offset)));
        qparams.add(new BasicNameValuePair("limit", String.valueOf(Constants.Soundcloud.PER_PAGE)));
        qparams.add(new BasicNameValuePair("q", query));
        return URIUtils.createURI(Constants.Soundcloud.API_SCHEME, Constants.Soundcloud.API_HOST, -1, Constants.Soundcloud.API_SEARCH, URLEncodedUtils.format(qparams, "UTF-8"), null);
    }

    /**
     * Used to build an mp3 skull search query. An initial request might be needed to fetch the
     * CSRF token from the page source before searching
     * @param query
     * @param csrf
     * @return
     * @throws URISyntaxException
     */
    public static URI getMp3SkullSearchURI(String query, String csrf) throws URISyntaxException{
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("q", query));
        qparams.add(new BasicNameValuePair("fckh", csrf));
        return URIUtils.createURI(Constants.Mp3skull.API_SCHEME, Constants.Mp3skull.API_HOST, -1, Constants.Mp3skull.API_SEARCH, URLEncodedUtils.format(qparams, "UTF-8"), null);
    }

    /**
     * Get the base Mp3Skull url to determine CSRF token for the session
     * @return
     * @throws URISyntaxException
     */
    public static URI getMp3SkullBaseURI() throws URISyntaxException{
        return URIUtils.createURI(Constants.Mp3skull.API_SCHEME, Constants.Mp3skull.API_HOST, -1, null, null, null);
    }

    /**
     * Prepare Vimeo search URL
     * @param query
     * @param page
     * @return
     * @throws URISyntaxException
     */
    public static URI getVimeoSearchURI(String query, Integer page) throws URISyntaxException {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("query", query));
        qparams.add(new BasicNameValuePair("page", String.valueOf(page)));
        qparams.add(new BasicNameValuePair("per_page", String.valueOf(Constants.Vimeo.PER_PAGE)));
        return URIUtils.createURI(Constants.Vimeo.API_SCHEME, Constants.Vimeo.API_HOST, -1, Constants.Vimeo.API_SEARCH, URLEncodedUtils.format(qparams, "UTF-8"), null);
    }

    /**
     * Prepare a valid Youtube API request URI
     * @param query
     * @param pageToken
     * @return
     * @throws URISyntaxException
     */
    public static URI getYoutubeSearchURI(String query, String pageToken) throws URISyntaxException{
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("q", query));
        //if there is a specific page to load
        if(pageToken != null && pageToken.length() > 0){
            qparams.add(new BasicNameValuePair("pageToken", pageToken));
        }
        qparams.add(new BasicNameValuePair("part", "snippet,id"));
        qparams.add(new BasicNameValuePair("maxResults", String.valueOf(Constants.Youtube.PER_PAGE)));
        qparams.add(new BasicNameValuePair("safeSearch", Constants.Youtube.SAFE_SEARCH));
        qparams.add(new BasicNameValuePair("type", Constants.Youtube.TYPE));
        qparams.add(new BasicNameValuePair("key", Constants.Youtube.API_TOKEN));
        return URIUtils.createURI(Constants.Youtube.API_SCHEME, Constants.Youtube.API_HOST, -1, Constants.Youtube.API_SEARCH, URLEncodedUtils.format(qparams, "UTF-8"), null);
    }

    /**
     * Break a list of strings into multiple lists of given max size partitionSize
     *
     * @param originalList  the original list
     * @param partitionSize max number of elements per list
     * @return a list of paritioned lists
     */
    public static List<List> partition(List originalList, int partitionSize) {
        List<List> partitions = new LinkedList<List>();
        for (int i = 0; i < originalList.size(); i += partitionSize) {
            partitions.add(originalList.subList(i, i + Math.min(partitionSize, originalList.size() - i)));
        }
        return partitions;
    }

    /**
     * Extract a list of urls from a string
     * Retrieved from: http://www.java-tutorial.ch/core-java-tutorial/extract-urls-using-java-regular-expressions
     *
     * @param value
     * @return
     */
    public static List<String> extractUrls(String value) {
        List<String> result = new ArrayList<String>();
        String urlPattern = "((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(value);
        while (m.find()) {
            result.add(value.substring(m.start(0), m.end(0)));
        }
        return result;
    }

}
