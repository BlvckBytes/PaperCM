package at.blvckbytes.paper_cm.config.section;

import at.blvckbytes.paper_cm.config.PostProcessedConfig;

public abstract class RootSection extends PostProcessedConfig {

  public EnvironmentSection env = new EnvironmentSection();

}
