package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldManager;
import org.gwtproject.uibinder.processor.MortalLogger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Managers access to all implementations of {@link AttributeParser}.
 */
public class AttributeParsers {

  private static final String HORIZ_CONSTANT = HorizontalAlignmentConstant.class.getCanonicalName();
  private static final String VERT_CONSTANT = VerticalAlignmentConstant.class.getCanonicalName();
  @SuppressWarnings("deprecation")
  private static final String TEXT_ALIGN_CONSTANT =
      com.google.gwt.user.client.ui.TextBoxBase.TextAlignConstant.class.getCanonicalName();
  private static final String INT = "int";
  private static final String STRING = String.class.getCanonicalName();
  private static final String DOUBLE = "double";
  private static final String BOOLEAN = "boolean";
  private static final String UNIT = Unit.class.getCanonicalName();
  private static final String SAFE_URI = SafeUri.class.getCanonicalName();

  private final MortalLogger logger;
  private final FieldReferenceConverter converter;

  /**
   * Class names of parsers keyed by method parameter signatures.
   */
  private final Map<String, AttributeParser> parsers = new HashMap<String, AttributeParser>();
  private final SafeUriAttributeParser safeUriInHtmlParser;

  public AttributeParsers(FieldManager fieldManager, MortalLogger logger) {
    this.logger = logger;
    converter = new FieldReferenceConverter(fieldManager);
    Types types = AptUtil.getTypeUtils();
    Elements elements = AptUtil.getElementUtils();

    BooleanAttributeParser boolParser = new BooleanAttributeParser(converter,
        types.getPrimitiveType(TypeKind.BOOLEAN), logger);
    addAttributeParser(BOOLEAN, boolParser);
    addAttributeParser(Boolean.class.getCanonicalName(), boolParser);

    IntAttributeParser intParser = new IntAttributeParser(converter,
        types.getPrimitiveType(TypeKind.BOOLEAN.INT), logger);
    addAttributeParser(INT, intParser);
    addAttributeParser(Integer.class.getCanonicalName(), intParser);

    DoubleAttributeParser doubleParser = new DoubleAttributeParser(converter,
        types.getPrimitiveType(TypeKind.DOUBLE), logger);
    addAttributeParser(DOUBLE, doubleParser);
    addAttributeParser(Double.class.getCanonicalName(), doubleParser);

    addAttributeParser("int,int", new IntPairAttributeParser(intParser,
        logger));

    addAttributeParser(HORIZ_CONSTANT, new HorizontalAlignmentConstantParser(
        converter, elements.getTypeElement(HORIZ_CONSTANT).asType(), logger));
    addAttributeParser(VERT_CONSTANT, new VerticalAlignmentConstantParser(
        converter, elements.getTypeElement(VERT_CONSTANT).asType(), logger));
    addAttributeParser(TEXT_ALIGN_CONSTANT, new TextAlignConstantParser(
        converter, elements.getTypeElement(TEXT_ALIGN_CONSTANT).asType(), logger));

    StringAttributeParser stringParser = new StringAttributeParser(converter,
        elements.getTypeElement(STRING).asType());
    addAttributeParser(STRING, stringParser);

    EnumAttributeParser unitParser = new EnumAttributeParser(converter,
        elements.getTypeElement(UNIT).asType(), logger);
    addAttributeParser(DOUBLE + "," + UNIT, new LengthAttributeParser(
        doubleParser, unitParser, logger));

    SafeUriAttributeParser uriParser = new SafeUriAttributeParser(stringParser,
        converter, elements.getTypeElement(SAFE_URI).asType(), logger);
    addAttributeParser(SAFE_URI, uriParser);

    safeUriInHtmlParser = new SafeUriAttributeParser(stringParser,
        converter, elements.getTypeElement(SAFE_URI).asType(),
        elements.getTypeElement(STRING).asType(), logger);

  }

  /**
   * Returns a parser for the given type(s). Accepts multiple types args to allow requesting parsers
   * for things like for pairs of ints.
   */
  public AttributeParser getParser(TypeMirror... types) {
    if (types.length == 0) {
      throw new RuntimeException("Asked for attribute parser of no type");
    }

    AttributeParser rtn = getForKey(getParametersKey(types));
    if (rtn != null || types.length > 1) {
      return rtn;
    }

    /* Maybe it's an enum */
    if (AptUtil.isEnum(types[0])) {
      return new EnumAttributeParser(converter, types[0], logger);
    }

    /*
     * Dunno what it is, so let a StrictAttributeParser look for a
     * {field.reference}
     */
    return new StrictAttributeParser(converter, logger, types[0]);
  }

  /**
   * Returns a parser specialized for handling URI references in html contexts, like &lt;a
   * href="{foo.bar}">.
   */
  public AttributeParser getSafeUriInHtmlParser() {
    return safeUriInHtmlParser;
  }

  private void addAttributeParser(String signature,
      AttributeParser attributeParser) {
    parsers.put(signature, attributeParser);
  }

  private AttributeParser getForKey(String key) {
    return parsers.get(key);
  }

  /**
   * Given a types array, return a key for the attributeParsers table.
   */
  private String getParametersKey(TypeMirror[] types) {
    StringBuffer b = new StringBuffer();
    for (TypeMirror t : types) {
      if (b.length() > 0) {
        b.append(',');
      }
      b.append(AptUtil.asTypeElement(t).getQualifiedName().toString());
      //FIXME b.append(t.getParameterizedQualifiedSourceName());
    }
    return b.toString();
  }
}
