package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.AbsolutePanel AbsolutePanel} widgets.
 */
public class AbsolutePanelParser implements ElementParser {

  private static final String AT = "at";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      // Parse position element.
      if (isPositionElement(elem, child)) {
        // Parse position.
        String left = child.consumeRequiredIntAttribute("left");
        String top = child.consumeRequiredIntAttribute("top");
        // Add child widget.
        XMLElement widgetElem = child.consumeSingleChildElement();
        FieldWriter widgetField = writer.parseElementToField(widgetElem);
        writer.addStatement("%1$s.add(%2$s, %3$s, %4$s);", fieldName,
            widgetField.getNextReference(), left, top);
        continue;
      }

      // Parse Widget.
      if (writer.isWidgetElement(child)) {
        FieldWriter widgetFieldName = writer.parseElementToField(child);
        writer.addStatement("%1$s.add(%2$s);", fieldName, widgetFieldName.getNextReference());
        continue;
      }

      // die
      writer.die(child, "Expecting only <%s:%s> or widget children in %s",
          elem.getPrefix(), AT, elem);
    }
  }

  private boolean isPositionElement(XMLElement parent, XMLElement child) {
    if (!parent.getNamespaceUri().equals(child.getNamespaceUri())) {
      return false;
    }
    if (!AT.equals(child.getLocalName())) {
      return false;
    }
    return true;
  }
}
