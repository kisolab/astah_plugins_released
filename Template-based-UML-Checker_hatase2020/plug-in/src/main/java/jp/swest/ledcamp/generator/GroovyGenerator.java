package jp.swest.ledcamp.generator;

import com.change_vision.jude.api.inf.model.IClass;
import com.google.common.base.Objects;
import groovy.lang.MissingPropertyException;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jp.swest.ledcamp.exception.GenerationException;
import jp.swest.ledcamp.generator.GeneratorUtils;
import jp.swest.ledcamp.generator.ITemplateEngine;
import jp.swest.ledcamp.xtendhelper.Using;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.INamedElement;

@SuppressWarnings("all")
public class GroovyGenerator implements ITemplateEngine {
  private SimpleTemplateEngine engine;
  
  public GroovyGenerator() {
    SimpleTemplateEngine _simpleTemplateEngine = new SimpleTemplateEngine();
    this.engine = _simpleTemplateEngine;
  }
  
  @Override
  public void doGenerate(final Map<String, Object> map, final Path output, final Path templateFile) {
    Object _get = map.get("u");
    final GeneratorUtils utils = ((GeneratorUtils) _get);
    final IClass iClass = utils.getIclass();
    System.out.println("GroovyGenerator " + iClass);
    try {
      final Template template = this.engine.createTemplate(templateFile.toFile());
      final Writable generatedCode = template.make(map);
      File _file = output.toFile();
      FileWriter _fileWriter = new FileWriter(_file);
      final Function1<FileWriter, Writer> _function = (FileWriter it) -> {
        try {
          return generatedCode.writeTo(it);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      Using.<FileWriter, Writer>using(_fileWriter, _function);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        GenerationException.getInstance().addException(e);
        boolean _matched = false;
        if (Objects.equal(e, MissingPropertyException.class)) {
          _matched=true;
          JFrame _frame = utils.getFrame();
          String _message = e.getMessage();
          String _plus = ("Cannot found property :" + _message);
          String _plus_1 = (_plus + ". model : ");
          String _name = iClass.getName();
          String _plus_2 = (_plus_1 + _name);
          JOptionPane.showMessageDialog(_frame, _plus_2);
        }
        if (!_matched) {
          JFrame _frame_1 = utils.getFrame();
          String _message_1 = e.getMessage();
          String _plus_3 = (_message_1 + ".\n in model : ");
          String _name_1 = iClass.getName();
          String _plus_4 = (_plus_3 + _name_1);
          JOptionPane.showMessageDialog(_frame_1, _plus_4);
        }
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
