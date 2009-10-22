package maps.gml;

import org.dom4j.Document;
import org.dom4j.Element;

public interface MapFormat {
    GMLMap read(Document doc);

    Document write(GMLMap map);

    boolean looksValid(Document doc);
}