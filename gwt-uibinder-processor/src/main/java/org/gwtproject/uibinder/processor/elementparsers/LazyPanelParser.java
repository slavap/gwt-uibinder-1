package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.LazyPanel} widgets.
 */
public class LazyPanelParser implements ElementParser {

  private static final String INITIALIZER_FORMAT = "new %s() {\n"
      + "  protected %s createWidget() {\n"
      + "    return %s;\n"
      + "  }\n"
      + "}";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    if (writer.getOwnerClass().getUiField(fieldName).isProvided()) {
      return;
    }

    if (!writer.useLazyWidgetBuilders()) {
      writer.die("LazyPanel only works with UiBinder.useLazyWidgetBuilders enabled.");
    }

    XMLElement child = elem.consumeSingleChildElement();
    if (!writer.isWidgetElement(child)) {
      writer.die(child, "Expecting only widgets in %s", elem);
    }

    FieldWriter childField = writer.parseElementToField(child);

    String lazyPanelClassPath = LazyPanel.class.getName();
    String widgetClassPath = Widget.class.getName();

    String code = String.format(INITIALIZER_FORMAT, lazyPanelClassPath,
        widgetClassPath, childField.getNextReference());
    writer.setFieldInitializer(fieldName, code);
  }
}
