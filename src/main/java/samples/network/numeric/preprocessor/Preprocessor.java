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
package samples.network.numeric.preprocessor;

import static com.luciad.util.ILcdFireEventMode.NO_EVENT;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.numeric.TLcdNumericGraphEncoder;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.network.graph.partition.TLcdLimitedEditablePartitionedGraph;

import samples.network.numeric.indexedmap.IntegerLongIndexedMapEncoder;
import samples.network.numeric.indexedmap.LongIntegerIndexedMapEncoder;

/**
 * Preprocessor for converting a set of SHP files representing a network into a numeric graph.
 * <p/>
 * This sample can be used as a starting point for setting up a custom preprocessor.
 * <p/>
 * The lines below describes how the preprocessor behaves in its default configuration and what
 * data input it expects in this case. The different actors in the preprocessor can be replaced
 * with custom components.
 * <p/>
 * The preprocessor will perform the following steps:
 * <ul>
 * <li>Decode all SHP files into models.</li>
 * <li>Convert each model to a graph.</li>
 * <li>Automatically convert each graph into a partitioned graph using the
 * {@code com.luciad.network.algorithm.partitioning.TLcdClusteredPartitioningAlgorithm}.</li>
 * <li>Assemble all these partitoned graphs into a single partitioned graph.</li>
 * <li>Export this graph to a numeric graph and write it to disk.</li>
 * <li>Build fast-accessible mapping files between nodes/edges and numerical id's.</li>
 * <li>Create a properties file linking all information together. This property file can be used
 * to load the numeric graph in the network.numeric.viewer sample.</li>
 * </li>
 * <p/>
 * The preprocessor expects a data directory with the following structure:
 * <p/>
 * <ul>
 * <li>For each top-level partition, there should be a subdirectory in the top-level data directory.
 * <li>There should be one subdirectory in the top-level data-directory with name 'boundaries', in
 * which the top-level boundary graph data are stored.</li>
 * <li>Within each partition and the boundary graph subdirectories, there should be edge and node files.</li>
 * <li>edge files: one or more SHP files containing all edges in the graph. These should end with '_edges.shp'</li>
 * <li>node files: one ore more SHP files containing all nodes in the graph. These should end with '_nodes.shp'</li>
 * <li>boundary property files: describe the bounds of all the SHP files. They should have the same name
 * as the SHP file they describe, but with suffix .bnd</li>
 * </ul>
 * <p/>
 * Note that the preprocessor is written in such a way that all top-level partitions are lazily created
 * when needed. This is to be able to preprocess large graphs which do not fit completely in memory.
 * In these cases, the user should perform a first partitioning himself, by splitting up the original
 * data (SHP) file(s) into multiple directories defining the top-level partitions, as described above.
 */
public class Preprocessor {

  public static final String DATA = "data";
  public static final String TOPOLOGY = "network.topology";
  public static final String VALUES = "network.values";
  public static final String NODE_2_GRAPH_INDEX = "network.node2GraphIndex";
  public static final String EDGE_2_GRAPH_INDEX = "network.edge2GraphIndex";
  public static final String GRAPH_2_EDGE_INDEX = "network.graph2EdgeIndex";

  // Model decoder for decoding the basic data files. This can be replaced with a custom model
  // decoder. The decoded models should contain the native node and edge objects.
  private ILcdModelDecoder fModelDecoder = new SHPModelDecoder();

  // Edge value function that computes for each edge in the native model a value. This should be kept
  // in sync with the models that are created by the model decoder.
  private ISimpleEdgeValueFunction fNativeEdgeValueFunction = new GeodeticEdgeValueFunction();

  // Graph factory that converts the decoded models into graphs. This can be replaced with a custom
  // graph factory. This should be kept in sync with the model that are created by the model decoder.
  private IGraphFactory fGraphFactory = new DataObjectIdGraphFactory(0, 0, 1, 2, fNativeEdgeValueFunction);

  // The edge value function that can compute for each edge in the graph a value. This should be kept
  // in sync with the edge objects that are created by the graph factory.
  private ILcdEdgeValueFunction fGraphEdgeValueFunction = new ValuedEdgeValueFunction();

  /**
   * Converts the data in the specified data directory into a numeric graph, and writes this to the
   * specified destination.
   *
   * @param aDataFile        the data to be converted into a numeric graph.
   * @param aDestinationFile the destination name of the numeric graph.
   *
   * @throws IOException if an I/O exception occurs during the preprocessing.
   */
  public void preprocess(String aDataFile,
                         String aDestinationFile) throws IOException {
    ILcdPartitionedGraph graph = loadGraph(aDataFile);
    if (graph == null) {
      System.out.println("No data to preprocess found in specified directory.");
      return;
    }
    String filename = TLcdIOUtil.getFileName(aDestinationFile);
    String tmpNodeMap = createTemporaryFileName(filename.replace(".ng", "_nodes.map.tmp"));
    String tmpEdgeMap = createTemporaryFileName(filename.replace(".ng", "_edges.map.tmp"));
    String tmpInverseEdgeMap = createTemporaryFileName(filename.replace(".ng", "_edges_inverse.map.tmp"));
    String nodeMap = aDestinationFile.replace(".ng", "_nodes.map");
    String edgeMap = aDestinationFile.replace(".ng", "_edges.map");
    String inverseEdgeMap = aDestinationFile.replace(".ng", "_edges_inverse.map");

    convertGraph(graph, aDestinationFile, tmpNodeMap, tmpEdgeMap, tmpInverseEdgeMap);
    buildIndex(tmpNodeMap, tmpEdgeMap, tmpInverseEdgeMap,
               nodeMap, edgeMap, inverseEdgeMap);

    new File(tmpNodeMap).delete();
    new File(tmpEdgeMap).delete();

    Properties properties = new Properties();
    properties.put(DATA, aDataFile);
    properties.put(TOPOLOGY, TLcdIOUtil.getFileName(aDestinationFile));
    properties.put(VALUES, TLcdIOUtil.getFileName(aDestinationFile.replace(".ng", ".ngv")));
    properties.put(NODE_2_GRAPH_INDEX, TLcdIOUtil.getFileName(nodeMap));
    properties.put(EDGE_2_GRAPH_INDEX, TLcdIOUtil.getFileName(edgeMap));
    properties.put(GRAPH_2_EDGE_INDEX, TLcdIOUtil.getFileName(inverseEdgeMap));
    properties.store(new FileOutputStream(aDestinationFile.replace(".ng", ".properties")), "Numeric graph properties");
  }

  /**
   * Loads the graph to be preprocessed.
   *
   * @param aDataDir the directory where the graph source data are located.
   *
   * @return the graph to be preprocessed.
   *
   * @throws IOException if an I/O exception occurs during the loading of the graph.
   */

  private ILcdPartitionedGraph loadGraph(String aDataDir) throws IOException {

    TLcdLimitedEditablePartitionedGraph graph = new TLcdLimitedEditablePartitionedGraph();

    // Load all partitions: all SHP files ending with '_edges.shp' (and '_nodes.shp').
    File[] edgeFiles = new File(aDataDir).listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.isDirectory()
               && !pathname.getAbsolutePath().endsWith("boundaries")
               && !pathname.getAbsolutePath().endsWith("CVS");
      }
    });

    if (edgeFiles == null) {
      return null;
    }

    for (File file : edgeFiles) {
      IGraphDecoder graphDecoder = new GraphDecoder(fModelDecoder, fGraphFactory);
      SoftPartitionedGraph partition = new SoftPartitionedGraph(file.getAbsolutePath(), graphDecoder);
      graph.addPartition(partition, NO_EVENT, NO_EVENT);
    }

    // Load the boundary graph: all SHP files ending with '_boundaries_edges.shp'.
    ILcdModel model = fModelDecoder.decode(new File(new File(aDataDir), "boundaries").getAbsolutePath());
    ILcdGraph boundaryGraph = fGraphFactory.createGraph(model);
    for (Enumeration edges = boundaryGraph.getEdges(); edges.hasMoreElements(); ) {
      Object edge = edges.nextElement();
      graph.addBoundaryEdge(edge,
                            boundaryGraph.getStartNode(edge),
                            boundaryGraph.getEndNode(edge),
                            NO_EVENT,
                            NO_EVENT);
    }
    return graph;
  }

  /**
   * Exports the specified graph to a numeric graph on disk.
   *
   * @param aGraph           the graph to be converted to a numeric graph.
   * @param aDestinationFile the destination file where the numeric graph should be written.
   * @param aNodeMapDest     the destination file where the node-to-id mapping should be written.
   * @param aEdgeMapDest     the destination file where the edge-to-id mapping should be written.
   *
   * @throws IOException if an I/O exception occurs during the export of the graph.
   */
  private void convertGraph(ILcdGraph aGraph,
                            String aDestinationFile,
                            String aNodeMapDest,
                            String aEdgeMapDest,
                            String aInverseEdgeMapDest) throws IOException {
    // Convert to numeric graph.
    IntegerIdNumericGraphMappingHandler mappingHandler = new IntegerIdNumericGraphMappingHandler(aNodeMapDest, aEdgeMapDest, aInverseEdgeMapDest);
    TLcdNumericGraphEncoder exporter = new TLcdNumericGraphEncoder();
    exporter.setNodeSplitThresholds(new int[]{-1, 400});
    exporter.exportGraph(aGraph, fGraphEdgeValueFunction, mappingHandler, aDestinationFile, aDestinationFile.replace(".ng", ".ngv"));
    mappingHandler.close();
  }

  /**
   * Builds a fast-accessible index and inverse index for the node-to-id and edge-to-id mapping.
   *
   * @param aTmpNodeMapSource   the location of the node mapping to be processed.
   * @param aTmpEdgeMapSource   the location of the edge mapping to be processed.
   * @param aNodeMapDest        the destination for the generated node index.
   * @param aEdgeMapDest        the destination for the generated edge index.
   * @param aInverseEdgeMapDest the destination for the generated inverse edge index.
   *
   * @throws IOException if an I/O exception occurs during the index creation.
   */
  private void buildIndex(String aTmpNodeMapSource,
                          String aTmpEdgeMapSource,
                          String aTmpInverseEdgeMapSource,
                          String aNodeMapDest,
                          String aEdgeMapDest,
                          String aInverseEdgeMapDest) throws IOException {
    IntegerLongIndexedMapEncoder nodeMapBuilder = new IntegerLongIndexedMapEncoder();
    nodeMapBuilder.buildIndexedMap(aTmpNodeMapSource, aNodeMapDest);
    IntegerLongIndexedMapEncoder edgeMapBuilder = new IntegerLongIndexedMapEncoder();
    edgeMapBuilder.buildIndexedMap(aTmpEdgeMapSource, aEdgeMapDest);
    LongIntegerIndexedMapEncoder inverseEdgeMapBuilder = new LongIntegerIndexedMapEncoder();
    inverseEdgeMapBuilder.buildIndexedMap(aTmpInverseEdgeMapSource, aInverseEdgeMapDest);
  }

  private static String createTemporaryFileName(String aFileName) {
    String tmpDir = System.getProperty("java.io.tmpdir");
    return new File(tmpDir, aFileName).getAbsolutePath();
  }

  /**
   * Main executable method.
   *
   * @param aArgs two arguments, specifying the data source dir and the
   *             numeric graph destination file.
   *
   * @throws IOException if an I/O exception occurs during the preprocessing.
   */
  public static void main(String[] aArgs) throws IOException {
    if (aArgs.length == 2) {
      // One data file + destination file
      try {
        new Preprocessor().preprocess(new File(aArgs[0]).getAbsolutePath(), aArgs[1]);
      } catch (Throwable t) {
        System.out.println("Error while preprocessing data: " + t.getMessage());
      }
    } else {
      System.out.println("Converts a directory with SHP files representing a network into a numeric graph.");
      System.out.println("Refer to the sample class for more information.");
      System.out.println("Arguments: <SHP-data-directory> <output-file-name>");
      System.exit(-1);
    }
  }

}
