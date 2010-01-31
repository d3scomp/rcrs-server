package gis2;

import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Civilian;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   This class knows how to read scenario files and apply them to StandardWorldModels.
*/
public class Scenario {
    private static final Log LOG = LogFactory.getLog(Scenario.class);

    private static final int DEFAULT_HP = 10000;
    private static final int DEFAULT_STAMINA = 10000;

    private static final String RCR_NAMESPACE_URI = "urn:roborescue:map:scenario";
    private static final Namespace RCR_NAMESPACE = DocumentHelper.createNamespace("rcr", RCR_NAMESPACE_URI);

    private static final QName RCR_ROOT_QNAME = DocumentHelper.createQName("scenario", RCR_NAMESPACE);
    private static final QName ID_QNAME = DocumentHelper.createQName("id", RCR_NAMESPACE);
    private static final QName LOCATION_QNAME = DocumentHelper.createQName("location", RCR_NAMESPACE);

    private static final XPath REFUGE_XPATH = DocumentHelper.createXPath("rcr:scenario/rcr:refuge");
    private static final XPath CIV_XPATH = DocumentHelper.createXPath("rcr:scenario/rcr:civilian");

    // Map from uri prefix to uri for XPaths
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private Collection<Integer> refugeIDs;
    private Collection<Integer> civLocations;

    static {
        URIS.put("rcr", RCR_NAMESPACE_URI);

        REFUGE_XPATH.setNamespaceURIs(URIS);
        CIV_XPATH.setNamespaceURIs(URIS);
    }

    /**
       Create a scenario from an XML document.
       @param doc The document to read.
    */
    public Scenario(Document doc) {
        refugeIDs = new HashSet<Integer>();
        civLocations = new HashSet<Integer>();
        for (Object next : REFUGE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            refugeIDs.add(Integer.parseInt(e.attributeValue(ID_QNAME)));
        }
        for (Object next : CIV_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            civLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
    }

    /**
       Apply this scenario to a world model.
       @param model The world model to alter.
    */
    public void apply(StandardWorldModel model) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("All entities before scenario: ");
            for (StandardEntity se : model) {
                LOG.debug(se.toString());
            }
        }
        for (int next : refugeIDs) {
            Building b = (Building)model.getEntity(new EntityID(next));
            Refuge r = new Refuge(b);
            model.removeEntity(b);
            if (LOG.isDebugEnabled()) {
                LOG.debug("All entities after removing building: ");
                for (StandardEntity se : model) {
                    LOG.debug(se.toString());
                }
            }
            model.addEntity(r);
            LOG.debug("Converted " + b + " into " + r);
            if (LOG.isDebugEnabled()) {
                LOG.debug("All entities after conversion: ");
                for (StandardEntity se : model) {
                    LOG.debug(se.toString());
                }
            }
        }
        int nextID = 0;
        for (StandardEntity next : model) {
            nextID = Math.max(nextID, next.getID().getValue());
        }
        for (int next : civLocations) {
            EntityID id = new EntityID(next);
            Civilian c = new Civilian(new EntityID(++nextID));
            Area position = (Area)model.getEntity(id);
            c.setX(position.getX());
            c.setY(position.getY());
            c.setPosition(id);
            c.setStamina(DEFAULT_STAMINA);
            c.setHP(DEFAULT_HP);
            c.setDamage(0);
            c.setBuriedness(0);
            c.setDirection(0);
            c.setTravelDistance(0);
            c.setPositionHistory(new int[0]);
            model.addEntity(c);
            LOG.debug("Created civilian " + c.getFullDescription());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("All entities after scenario applied: ");
            for (StandardEntity se : model) {
                LOG.debug(se.toString());
            }
        }
    }
}