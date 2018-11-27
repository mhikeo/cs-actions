package io.cloudslang.content.postgres.services;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

import static io.cloudslang.content.postgres.utils.Constants.*;
import static io.cloudslang.content.postgres.utils.Constants.WORK_MEM;


@SuppressWarnings("ALL")
public class ConfigService {

    public static Map<String, Object> validateAndBuildKeyValuesMap(String port, String ssl, String sslCaFile, String sslCertFile,
                                                                   String sslKeyFile, String maxConnections, String sharedBuffers,
                                                                   String effectiveCacheSize, String autovacuum, String workMem) {

        Map<String, Object> keyValues = new HashMap<>();
        if(StringUtils.isNumeric(port)) {
            keyValues.put(PORT, Integer.parseInt(port));
        }

        if(StringUtils.isNotEmpty(ssl)) {
            keyValues.put(SSL, ssl);
        }

        if(StringUtils.isNotEmpty(sslCaFile)) {
            keyValues.put(SSL_CA_FILE, sslCaFile);
        }

        if(StringUtils.isNotEmpty(sslCertFile)) {
            keyValues.put(SSL_CERT_FILE, sslCertFile);
        }

        if(StringUtils.isNotEmpty(sslKeyFile)) {
            keyValues.put(SSL_KEY_FILE, sslKeyFile);
        }

        if(StringUtils.isNumeric(maxConnections)) {
            keyValues.put(MAX_CONNECTIONS, Integer.parseInt(maxConnections));
        }

        if(StringUtils.isNotEmpty(sharedBuffers)) {
            keyValues.put(SHARED_BUFFERS, sharedBuffers);
        }

        if(StringUtils.isNotEmpty(effectiveCacheSize)) {
            keyValues.put(EFFECTIVE_CACHE_SIZE, effectiveCacheSize);
        }

        if(StringUtils.isNotEmpty(autovacuum)) {
            keyValues.put(AUTOVACUUM, autovacuum);
        }

        if(StringUtils.isNotEmpty(workMem)) {
            keyValues.put(WORK_MEM, workMem);
        }

        return keyValues;
    }

    public static void changeProperty(String filename, Map<String, Object> keyValuePairs) throws IOException {
        if(keyValuePairs.size() == 0) {
            return;
        }

        final File file = new File(filename);
        final File tmpFile = new File(file + ".tmp");

        PrintWriter pw = new PrintWriter(tmpFile);
        BufferedReader br = new BufferedReader(new FileReader(file));

        Set<String> keys = keyValuePairs.keySet();
        List<String> inConfig = new ArrayList<>();

        for (String line; (line = br.readLine()) != null; ) {
            int keyPos = line.indexOf('=');
            if (keyPos > -1) {
                String key = line.substring(0, keyPos).trim();

                if (!key.trim().startsWith("#") && keys.contains(key) && !inConfig.contains(key)) {
                    // Check if the line has any comments.  Split by '#' to funs all tokens.
                    String[] keyValuePair = line.split("=");

                    StringBuilder lineBuilder = new StringBuilder();
                    lineBuilder.append(keyValuePair[0].trim()).append(" = ").append(keyValuePairs.get(key));

                    line = lineBuilder.toString();
                    inConfig.add(key);
                }
            }
            pw.println(line);
        }

        for (String key : keys) {
            if (!inConfig.contains(key)) {
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(key).append(" = ").append(keyValuePairs.get(key));
                pw.println(lineBuilder.toString());
            }
        }

        br.close();
        pw.close();
        file.delete();
        tmpFile.renameTo(file);
    }

    public static void changeProperty(String filename, String[] allowedHosts, String[] allowedUsers) throws IOException {

        if ((allowedHosts == null || allowedHosts.length == 0) && (allowedUsers == null || allowedUsers.length == 0)) {
            return;
        }

        final File file = new File(filename);
        final File tmpFile = new File(file + ".tmp");

        PrintWriter pw = new PrintWriter(tmpFile);
        BufferedReader br = new BufferedReader(new FileReader(file));

        Object[] allowedArr = new Object[allowedHosts.length * allowedUsers.length];
        boolean[] skip = new boolean[allowedArr.length];
        int ctr = 0;
        for(int i = 0; i < allowedHosts.length; i++){
            for (int j = 0; j < allowedUsers.length; j++) {
                allowedArr[ctr++] = new String[] { allowedHosts[i], allowedUsers[j] };
            }
        }

        for (String line; (line = br.readLine()) != null; ) {
            if (line.startsWith("host")) {
                for (int x = 0; x < allowedArr.length; x++) {
                    if (!skip[x]) {
                        String[] allowedItem = (String[]) allowedArr[x];
                        if (line.contains(allowedItem[0]) && line.contains(allowedItem[1])) {
                            skip[x] = true;
                            break;
                        }
                    }
                }
            }
            pw.println(line);
        }

        StringBuilder addUserHostLineBuilder  = new StringBuilder();
        for (int x = 0; x < allowedArr.length; x++) {
            if (!skip[x]) {
                String[] allowedItem = (String[]) allowedArr[x];
                addUserHostLineBuilder.append("host").append("\t").append("all").append("\t").append(allowedItem[1])
                        .append("\t").append(allowedItem[0]).append("\t").append("trust").append("\n");
            }
        }
        pw.write(addUserHostLineBuilder.toString());

        br.close();
        pw.close();
        file.delete();
        tmpFile.renameTo(file);

    }

}
