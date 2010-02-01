package rescuecore2.standard.kernel;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

import java.util.List;

import rescuecore2.view.EntityInspector;
import rescuecore2.view.ViewListener;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
//import rescuecore2.config.Config;
import rescuecore2.Timestep;
import rescuecore2.GUIComponent;

import rescuecore2.standard.view.StandardWorldModelViewer;

import kernel.Kernel;
import kernel.KernelListenerAdapter;

/**
   A KernelGUIComponent that will view a standard world model.
*/
public class StandardWorldModelViewerComponent extends KernelListenerAdapter implements GUIComponent {
    private static final int SIZE = 500;

    private StandardWorldModelViewer viewer;
    private EntityInspector inspector;
    private JComponent view;

    public StandardWorldModelViewerComponent() {
        viewer = new StandardWorldModelViewer();
        inspector = new EntityInspector();
        viewer.setPreferredSize(new Dimension(SIZE, SIZE));
        viewer.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
                    for (RenderedObject next : objects) {
                        if (next.getObject() instanceof Entity) {
                            inspector.inspect((Entity)next.getObject());
                            return;
                        }
                    }
                }

                @Override
                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
                }
            });
        view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer, new JScrollPane(inspector));
    }

    @Override
    public void simulationStarted(Kernel kernel) {
        viewer.initialise(kernel.getConfig());
        viewer.view(kernel.getWorldModel());
        viewer.repaint();
    }

    @Override
    public void timestepCompleted(Kernel kernel, Timestep time) {
        viewer.view(kernel.getWorldModel(), time.getCommands(), time.getChangeSet());
        viewer.repaint();
    }

    @Override
    public JComponent getGUIComponent() {
        return view;
    }

    @Override
    public String getGUIComponentName() {
        return "World view";
    }
}
