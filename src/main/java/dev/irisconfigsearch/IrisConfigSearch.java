package dev.irisconfigsearch;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IrisConfigSearch.MOD_ID)
public final class IrisConfigSearch {
    public static final String MOD_ID = "irisconfigsearch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IrisConfigSearch() {
        LOGGER.info("Iris Config Search initialized. Shader configuration integration is not yet implemented.");
    }
}
