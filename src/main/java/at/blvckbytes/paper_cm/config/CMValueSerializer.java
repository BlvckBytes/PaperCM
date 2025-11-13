package at.blvckbytes.paper_cm.config;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class CMValueSerializer implements ObjectSerializer<CMValue> {

  @Override
  public boolean supports(@NonNull Class<?> type) {
    return CMValue.class.isAssignableFrom(type);
  }

  @Override
  public void serialize(@NonNull CMValue object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
    data.setValue(object.rawValue);
  }

  @Override
  public CMValue deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
    var rawValue = data.getValueRaw();

    if (rawValue == null)
      return null;

    try {
      var constructor = generics.getType().getDeclaredConstructor(Object.class);
      constructor.setAccessible(true);
      return (CMValue) constructor.newInstance(rawValue);
    } catch (Throwable e) {
      throw new IllegalStateException(generics.getType() + " is missing constructor(Object)", e);
    }
  }
}
