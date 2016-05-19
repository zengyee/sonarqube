package org.sonar.server.computation.posttask;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import org.sonar.api.ce.posttask.ScannerContext;
import org.sonar.core.util.CloseableIterator;
import org.sonar.scanner.protocol.output.ScannerReport;

@Immutable
class ScannerContextImpl implements ScannerContext {

  private final Map<String, String> props;

  private ScannerContextImpl(Map<String, String> props) {
    this.props = props;
  }

  @Override
  public Map<String, String> getProperties() {
    return props;
  }

  static ScannerContextImpl from(CloseableIterator<ScannerReport.ContextProperty> it) {
    try {
      ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
      while (it.hasNext()) {
        ScannerReport.ContextProperty prop = it.next();
        mapBuilder.put(prop.getKey(), prop.getValue());
      }
      return new ScannerContextImpl(mapBuilder.build());
    } finally {
      it.close();
    }
  }
}
