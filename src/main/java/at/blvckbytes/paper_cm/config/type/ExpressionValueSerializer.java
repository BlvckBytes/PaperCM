package at.blvckbytes.paper_cm.config.type;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class ExpressionValueSerializer implements ObjectSerializer<ExpressionValue> {

  @Override
  public boolean supports(@NonNull Class<?> type) {
    return ExpressionValue.class.isAssignableFrom(type);
  }

  @Override
  public void serialize(@NonNull ExpressionValue object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
    data.setValue(object.rawValue);
  }

  @Override
  public ExpressionValue deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
    var rawValue = data.getValueRaw();

    if (rawValue == null)
      return null;

    try {
      var constructor = generics.getType().getDeclaredConstructor(Object.class);
      constructor.setAccessible(true);
      return (ExpressionValue) constructor.newInstance(rawValue);
    } catch (Throwable e) {
      throw new IllegalStateException(generics.getType() + " is missing constructor(Object)", e);
    }
  }
}
