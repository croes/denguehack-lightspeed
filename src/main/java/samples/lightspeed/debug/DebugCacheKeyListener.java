/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.lightspeed.debug;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import com.luciad.util.collections.ILcdMultiKeyMap;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.services.glcache.ALspGLResource;
import com.luciad.view.lightspeed.util.opengl.glsl.TLspShaderProgram;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Key listener that can be used to debug the central caching mechanism.
 * <ul>
 *   <li><b>g</b> opens a new frame with a {@code JTree} containing the current contents of the cache.</li>
 *   <li><b>c</b> after <b>g</b> invokes a {@code System.gc()}.</li>
 *   <li><b>p</b> prints the central cache contents to the logger.</li>
 *   <li><b>r</b> removes all shader programs from the cache (causing them to be reloaded).</li>
 * </ul>
 */
public class DebugCacheKeyListener extends KeyAdapter {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(DebugCacheKeyListener.class);

  private char fPreviousKey = ' ';

  private ILspView fView;
  private ILcdMultiKeyMap fCache;
  private char fKey;

  public DebugCacheKeyListener(ILspView aView, ILcdMultiKeyMap aCache, char aKey) {
    fView = aView;
    fCache = aCache;
    fKey = aKey;
  }

  private Object[] fDebugKey = new Object[]{"test", "branch"};

  @Override
  public void keyTyped(KeyEvent e) {
    // Perform a garbage collect.
    if (e.getKeyChar() == 'c' && fPreviousKey == 'g') {
      LOGGER.info("Performing Garbage Collect");
      System.gc();
    }
    // Print the contents of the cache to std out.
    if (e.getKeyChar() == 'p') {
      fView.addViewListener(new ALspViewAdapter() {
        @Override
        public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
          LOGGER.info(fCache.toString());
          fView.removeViewListener(this);
        }
      });
    }
    // Create a frame with a tree showing the current contents of the cache
    if (e.getKeyChar() == fKey) {
      fView.addViewListener(new ALspViewAdapter() {
        @Override
        public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
          createCacheFrame();
          fView.removeViewListener(this);
        }
      });
    }

    if (fKey == 'g') {

      if (e.getKeyChar() == 's') {
        fView.addViewListener(new ALspViewAdapter() {
          @Override
          public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
            Object[] key = new Object[]{fDebugKey[0], fDebugKey[1], new Object()};
            fCache.put(key, new DummyGLResource("dummy resource", 10 * 1024 * 1024));
            fView.removeViewListener(this);
          }
        });
      }

      if (e.getKeyChar() == 'c') {
        fView.addViewListener(new ALspViewAdapter() {
          @Override
          public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
            fCache.remove(fDebugKey);
            fView.removeViewListener(this);
          }
        });

      }

      if (e.getKeyChar() == 'q') {
        fView.addViewListener(new ALspViewAdapter() {
          @Override
          public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
            fCache.clearRecursive();
            fView.removeViewListener(this);
          }
        });
      }

      if (e.getKeyChar() == 'r') {
        fView.addViewListener(new ALspViewAdapter() {
          @Override
          public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
            removeShaders(fCache, "");
            fView.removeViewListener(this);
          }
        });
      }
    }

    fPreviousKey = e.getKeyChar();
  }

  public static void removeShaders(ILcdMultiKeyMap aCache, String aParentKey) {
    List<Object> keysToRemove = new ArrayList<Object>();
    Set<Map.Entry> entries = aCache.entrySet();
    for (Map.Entry entry : entries) {
      if (isShader(entry.getValue())) {
        keysToRemove.add(entry.getKey());
      }
    }
    for (Object key : keysToRemove) {
      LOGGER.info("Disposing shader [" + aParentKey + key + "]");
      aCache.remove(key);
    }
    Set<Map.Entry<Object, ILcdMultiKeyMap>> branches = aCache.branchEntrySet();
    for (Map.Entry<Object, ILcdMultiKeyMap> entry : branches) {
      removeShaders(entry.getValue(), aParentKey + entry.getKey() + "/");
    }
  }

  private static boolean isShader(Object aValue) {
    if (!(aValue instanceof ALspGLResource)) {
      return false;
    }
    if (aValue instanceof TLspShaderProgram) {
      return true;
    }
    return ((ALspGLResource) aValue).getSourceString().toLowerCase().contains("shader");
  }

  ////////////////////

  private JTree fTree;

  private JTree createTree() {
    JTree tree = DebugCacheJTreeFactory.getAsJTree(fCache);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    fTree = tree;
    return fTree;

  }

  private void createCacheFrame() {
    final JTree tree = createTree();
    final JFrame frame = new JFrame("Cache");
    frame.getContentPane().setLayout(new BorderLayout());

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(new JScrollPane(tree), BorderLayout.CENTER);

    final JButton updateButton = new JButton();
    updateButton.setAction(new AbstractAction("Update") {
      @Override
      public void actionPerformed(ActionEvent e) {
        fView.addViewListener(new ALspViewAdapter() {
          @Override
          public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
            final JTree tree = createTree();
            mainPanel.removeAll();
            mainPanel.add(new JScrollPane(tree), BorderLayout.CENTER);
            frame.validate();
            fView.removeViewListener(this);
          }
        });
      }
    });

    JButton gcAndUpdateButton = new JButton();
    gcAndUpdateButton.setAction(new AbstractAction("GC & Update") {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.gc();
        updateButton.doClick();
      }
    });

    final JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
    buttonsPanel.add(updateButton);
    buttonsPanel.add(gcAndUpdateButton);

    frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
    frame.add(buttonsPanel, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

}
