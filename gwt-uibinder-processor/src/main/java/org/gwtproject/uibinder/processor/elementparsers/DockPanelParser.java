package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.DockPanel} widgets.
 */
public class DockPanelParser implements ElementParser {

  private static final String TAG_DOCK = "Dock";
  private static final HashMap<String, String> values =
      new HashMap<String, String>();

  static {
    values.put("NORTH", "com.google.gwt.user.client.ui.DockPanel.NORTH");
    values.put("SOUTH", "com.google.gwt.user.client.ui.DockPanel.SOUTH");
    values.put("EAST", "com.google.gwt.user.client.ui.DockPanel.EAST");
    values.put("WEST", "com.google.gwt.user.client.ui.DockPanel.WEST");
    values.put("CENTER", "com.google.gwt.user.client.ui.DockPanel.CENTER");
    values.put("LINE_START", "com.google.gwt.user.client.ui.DockPanel.LINE_START");
    values.put("LINE_END", "com.google.gwt.user.client.ui.DockPanel.LINE_END");
  }

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      // DockPanel can only contain Dock elements.
      String ns = child.getNamespaceUri();
      String tagName = child.getLocalName();

      if (!ns.equals(elem.getNamespaceUri())) {
        writer.die(elem, "Invalid DockPanel child namespace: " + ns);
      }
      if (!tagName.equals(TAG_DOCK)) {
        writer.die(elem, "Invalid DockPanel child element: " + tagName);
      }

      // And they must specify a direction.
      if (!child.hasAttribute("direction")) {
        writer.die(elem, "Dock must specify the 'direction' attribute");
      }
      String value = child.consumeRawAttribute("direction");
      String translated = values.get(value);
      if (translated == null) {
        writer.die(elem, "Invalid value: dockDirection='" + value + "'");
      }

      // And they can only have a single child widget.
      XMLElement widget = child.consumeSingleChildElement();
      FieldWriter childField = writer.parseElementToField(widget);
      writer.addStatement("%1$s.add(%2$s, %3$s);", fieldName,
          childField.getNextReference(), translated);

      // Parse the CellPanel-specific attributes on the Dock element.
      CellPanelParser.parseCellAttributes(child, fieldName, childField, writer);
    }
  }
}
