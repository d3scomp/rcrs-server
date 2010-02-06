package maps.gml;

import maps.gml.formats.RobocupFormat;
import maps.gml.formats.OrdnanceSurveyFormat;
import maps.gml.formats.MeijoFormat;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A class for reading GML maps.
*/
public final class MapReader {
    private static final Log LOG = LogFactory.getLog(MapReader.class);

    private MapReader() {
    }

    /**
       Read a GMLMap from a file.
       @param file The name of the file to read.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(String file) throws GMLException {
        return readGMLMap(file, null);
    }

    /**
       Read a GMLMap from a file using a particular format.
       @param file The name of the file to read.
       @param format The format to use. If this is null then the format will be guessed.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(String file, MapFormat format) throws GMLException {
        try {
            return readGMLMap(new FileReader(new File(file)), format);
        }
        catch (FileNotFoundException e) {
            throw new GMLException(e);
        }
    }

    /**
       Read a GMLMap from a file.
       @param file The file to read.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(File file) throws GMLException {
        return readGMLMap(file, null);
    }

    /**
       Read a GMLMap from a file using a particular format.
       @param file The file to read.
       @param format The format to use. If this is null then the format will be guessed.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(File file, MapFormat format) throws GMLException {
        try {
            return readGMLMap(new FileReader(file), format);
        }
        catch (FileNotFoundException e) {
            throw new GMLException(e);
        }
    }

    /**
       Read a GMLMap from a Reader.
       @param in The Reader to read.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(Reader in) throws GMLException {
        return readGMLMap(in, null);
    }

    /**
       Read a GMLMap from a Reader using a particular format.
       @param in The Reader to read.
       @param format The format to use. If this is null then the format will be guessed.
       @return A GMLMap.
       @throws GMLException If there is a problem reading the map.
    */
    public static GMLMap readGMLMap(Reader in, MapFormat format) throws GMLException {
        try {
            LOG.info("Reading GML file");
            SAXReader reader = new SAXReader();
            Document doc = reader.read(in);
            if (format == null) {
                LOG.debug("Guessing format");
                format = guessFormat(doc);
            }
            if (format == null) {
                throw new GMLException("Unrecognised format");
            }
            LOG.debug("Parsing " + format.toString() + " format");
            return format.read(doc);
        }
        catch (DocumentException e) {
            throw new GMLException(e);
        }
    }

    private static MapFormat guessFormat(Document doc) {
        List<MapFormat> all = new ArrayList<MapFormat>();
        all.add(new RobocupFormat());
        all.add(new MeijoFormat());
        all.add(new OrdnanceSurveyFormat());
        for (MapFormat next : all) {
            if (next.looksValid(doc)) {
                return next;
            }
        }
        return null;
    }
}