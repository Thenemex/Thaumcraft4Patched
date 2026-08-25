package thaumcraft4patched.model.patch;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import thaumcraft4patched.config.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

/**
 * Makes the Gravestone death note obey the config of its own mod.
 *
 * Gravestone hands out a death note from two places. The drop handler reads
 * the "enable_death_note" entry before it gives the note. The death handler
 * that runs when the keepInventory game rule is on never reads that entry. A
 * server with keepInventory on therefore gives a note after every death, even
 * when the entry is off.
 *
 * The transformer sends the inventory call of that second path to this class.
 * The note is dropped when the mod config asks for no notes.
 */
public final class GravestoneDeathNotePatch implements IPatch {

    private static final String GRAVESTONE_CONFIG_CLASS =
            "de.maxhenkel.gravestone.Config";

    private static final String INSTANCE_METHOD = "instance";

    private static final String GIVE_DEATH_NOTES_FIELD = "giveDeathNotes";

    private static Boolean cachedSetting;

    private GravestoneDeathNotePatch() {}

    /**
     * Replaces the inventory call that stores the death note.
     *
     * @return true when the note was stored, as the original call does. The
     *         caller drops this value.
     */
    public static boolean addDeathNote(
            InventoryPlayer inventory,
            ItemStack note) {

        if (!givesDeathNotes()) {
            return false;
        }

        return inventory.addItemStackToInventory(note);
    }

    private static boolean givesDeathNotes() {
        if (!Config.gravestoneEnabled
                || !Config.deathNoteKeepInventoryPatchEnabled) {

            return true;
        }

        if (cachedSetting == null) {
            cachedSetting = readModConfig();
        }

        return cachedSetting;
    }

    /**
     * Reads "enable_death_note" from the Gravestone mod.
     *
     * The mod holds that entry in a field of its own config class. This addon
     * does not build against Gravestone, so the read goes through reflection.
     * A read that fails leaves the note in place.
     */
    private static boolean readModConfig() {
        try {
            Class<?> configClass =
                    Class.forName(GRAVESTONE_CONFIG_CLASS);

            Method instance =
                    configClass.getMethod(INSTANCE_METHOD);

            Field giveDeathNotes =
                    configClass.getField(GIVE_DEATH_NOTES_FIELD);

            return giveDeathNotes.getBoolean(instance.invoke(null));
        } catch (Throwable error) {
            logger.error(
                    "Could not read the Gravestone death note setting. "
                            + "Death notes are left on.",
                    error
            );

            return true;
        }
    }
}
