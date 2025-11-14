package at.blvckbytes.paper_cm.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class LineNumberResolver {

  private final List<String> fileLines;
  private final Node rootNode;

  public LineNumberResolver(File configFile) throws Exception {
    this.fileLines = new ArrayList<>();

    try (var scanner = new Scanner(configFile)) {
      while (scanner.hasNextLine())
        fileLines.add(scanner.nextLine());
    }

    try (var reader = new FileReader(configFile)) {
      this.rootNode = new Yaml().compose(reader);
    }
  }

  public LineNumbers resolve(SequencedCollection<String> pathParts) {
    var currentNode = rootNode;
    Node lastKey = null;

    var remainingPathParts = new ArrayList<>(pathParts);

    partLoop: while (!remainingPathParts.isEmpty()) {
      var key = remainingPathParts.removeFirst();

      if (currentNode instanceof MappingNode mappingNode) {
        for (var nodeTuple : mappingNode.getValue()) {
          if (!(nodeTuple.getKeyNode() instanceof ScalarNode keyNode))
            continue;

          if (!keyNode.getValue().equals(key))
            continue;

          currentNode = nodeTuple.getValueNode();
          lastKey = keyNode;
          continue partLoop;
        }

        throw new IllegalStateException("Could not locate path-part \"" + key + "\" of path " + String.join(".", pathParts));
      }

      if (currentNode instanceof CollectionNode<?> collectionNode) {
        int index;

        try {
          index = Integer.parseInt(key);
        } catch (NumberFormatException e) {
          throw new IllegalStateException("Encountered non-numeric path-part of a collection: \"" + key + "\"");
        }

        if (index < 0 || index >= collectionNode.getValue().size())
          throw new IllegalStateException("Encountered out-of-range path-part of a collection: \"" + key + "\"");

        currentNode = (Node) collectionNode.getValue().get(index);
        lastKey = collectionNode;
        continue;
      }

      throw new IllegalStateException("Encountered unaccounted-for node-type: " + currentNode.getClass());
    }

    if (lastKey == null)
      throw new IllegalStateException("Cannot resolve line-numbers of an empty path");

    var keyLineIndex = lastKey.getStartMark().getLine();
    var valueLineIndex = currentNode.getStartMark().getLine();

    if (currentNode instanceof ScalarNode scalarNode) {
      var style = scalarNode.getScalarStyle();

      if (style == DumperOptions.ScalarStyle.FOLDED || style == DumperOptions.ScalarStyle.LITERAL)
        ++valueLineIndex;
    }

    return new LineNumbers(
      keyLineIndex + 1,
      fileLines.get(keyLineIndex),
      valueLineIndex + 1,
      fileLines.get(valueLineIndex)
    );
  }
}
