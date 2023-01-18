package io.github.moremcmeta.propertiesreaderplugin.forge;

import io.github.moremcmeta.propertiesreaderplugin.ModConstants;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

/**
 * Mod entrypoint on Forge.
 * @author soir20
 */
@Mod(ModConstants.MOD_ID)
@SuppressWarnings("unused")
public class EntrypointForge {

    /**
     * Serves as mod entrypoint on Forge and tells the server to ignore this mod.
     */
    public EntrypointForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                ()-> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer)-> true
                )
        );

    }

}
