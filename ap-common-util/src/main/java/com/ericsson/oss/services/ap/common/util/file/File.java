/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.file;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * POJO model for File with name and content.
 */
public class File {

    public static final String NAME_KEY = "name";
    public static final String CONTENT_KEY = "content";

    private String name;
    private String content;

    public File() {
    } //Needed for JSON Mapping

    public File(final String name, final String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Creates a file from a given map of String, Object
     *
     * @param fileMap a map of String, Object
     * @return an instance of File
     */
    public static File fromMap(Map<String, Object> fileMap) {
        if (fileMap == null) {
            return null;
        }

        File file = new File();

        file.setName((String) fileMap.getOrDefault(NAME_KEY, "undefined_file_name"));
        file.setContent((String) fileMap.getOrDefault(CONTENT_KEY, null));

        return file;
    }

    /**
     * Converts a list of map(name, content) to a list of Files
     *
     * @param fileMapList a list of map (name, content)
     * @return a list of files
     */
    public static List<File> fromMapList(List<Map<String, Object>> fileMapList) {
        return fileMapList == null ?
            null :
            fileMapList
                .stream()
                .map(File::fromMap)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns a map (name, content) from a given file
     *
     * @param file a file containing text or a graphic
     * @return a map of name,content
     */
    public static Map<String, Object> toMap(final File file) {
        final Map<String, Object> fileMap = new HashMap<>(2);
        if (file != null) {
            fileMap.put("name", file.getName());
            fileMap.put("content", file.getContent());
        }
        return fileMap;
    }

    /**
     * Returns a map (name, content) where the content is base64 encoded, from a given file name and location
     *
     * @param location folder path where the file should be fetched.
     * @param fileName the file name
     * @return a map of name,content
     */
    public static Map<String, Object> toBase64EncodedMap(final String location, final String fileName) {
        final String fullQualifiedFilePath = String.format("%s/%s", location, fileName);
        ResourceService resourceService = new ServiceFinderBean().find(ResourceService.class);
        final byte[] fileContent = resourceService.getBytes(fullQualifiedFilePath);
        final String base64Content = encodeBase64FileContent(fileContent);

        final Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("name", fileName);
        fileMap.put("content", base64Content);
        return fileMap;
    }

    /**
     * Encodes a byte array to base64 String.
     *
     * @param fileContent byte[]
     * @return base64 encoded String
     */
    public static String encodeBase64FileContent(final byte[] fileContent) {
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Decodes a base64 String to byte array
     *
     * @param base64EncodedContent {@link String} in base64
     * @return byte[]
     */
    public static byte[] decodeBase64FileContent(final String base64EncodedContent) {
        return Base64.getDecoder().decode(base64EncodedContent);
    }
}
