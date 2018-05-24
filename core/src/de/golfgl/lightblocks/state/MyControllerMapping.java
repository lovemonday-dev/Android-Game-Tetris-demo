package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.gdx.controllers.mapping.ConfiguredInput;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import de.golfgl.gdx.controllers.mapping.ControllerToInputAdapter;
import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 05.11.2017.
 */
public class MyControllerMapping extends ControllerMappings {

    public static final int BUTTON_ROTATE_CLOCKWISE = 0;
    public static final int BUTTON_ROTATE_COUNTERCLOCK = 1;
    public static final int BUTTON_HARDDROP = 6;
    public static final int BUTTON_HOLD = 7;
    public static final int BUTTON_FREEZE = 8;
    public static final int AXIS_VERTICAL = 2;
    public static final int AXIS_HORIZONTAL = 3;
    public static final int BUTTON_START = 4;
    public static final int BUTTON_CANCEL = 5;
    public ControllerToInputAdapter controllerToInputAdapter;
    public boolean loadedSavedSettings;

    private Controller controllerInUse;

    public MyControllerMapping(LightBlocksGame app) {
        super();

        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_ROTATE_CLOCKWISE));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_ROTATE_COUNTERCLOCK));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_START));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_CANCEL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_VERTICAL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_HORIZONTAL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_HARDDROP));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_HOLD));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_FREEZE));

        commitConfig();
        loadedSavedSettings = false;

        try {
            String json = app.localPrefs.loadControllerMappings();
            JsonValue jsonValue = new JsonReader().parse(json);
            if (jsonValue != null)
                loadedSavedSettings = fillFromJson(jsonValue);
        } catch (Throwable t) {
            Gdx.app.error("Prefs", "Error reading saved controller mappings", t);
        }

        controllerToInputAdapter = new MyControllerToInputAdapter(this);
    }

    public void setInputProcessor(InputProcessor input) {
        controllerToInputAdapter.setInputProcessor(input);
    }

    public Controller getControllerInUse() {
        // returns a controller currently in use while MyControllerToInputAdapter.sendKeyToTarget fires
        return controllerInUse;
    }

    @Override
    public boolean getDefaultMapping(MappedInputs defaultMapping, Controller controller) {
        ControllerMapping controllerMapping = controller.getMapping();

        defaultMapping.putMapping(new MappedInput(AXIS_VERTICAL, new ControllerAxis(controllerMapping.axisLeftY)));
        defaultMapping.putMapping(new MappedInput(AXIS_HORIZONTAL, new ControllerAxis(controllerMapping.axisLeftX)));
        defaultMapping.putMapping(new MappedInput(BUTTON_ROTATE_CLOCKWISE, new ControllerButton(controllerMapping.buttonB)));
        defaultMapping.putMapping(new MappedInput(BUTTON_ROTATE_COUNTERCLOCK, new ControllerButton(controllerMapping.buttonA)));
        defaultMapping.putMapping(new MappedInput(BUTTON_HARDDROP, new ControllerButton(controllerMapping.buttonX)));
        defaultMapping.putMapping(new MappedInput(BUTTON_HOLD, new ControllerButton(controllerMapping.buttonY)));
        defaultMapping.putMapping(new MappedInput(BUTTON_FREEZE, new ControllerButton(controllerMapping.buttonR2)));
        defaultMapping.putMapping(new MappedInput(BUTTON_START, new ControllerButton(controllerMapping.buttonStart)));
        defaultMapping.putMapping(new MappedInput(BUTTON_CANCEL, new ControllerButton(controllerMapping.buttonBack)));

        return true;
    }

    public boolean hasHardDropMapping(Controller controller) {
        return getControllerMapping(controller).getMappedInput(BUTTON_HARDDROP) != null;
    }

    /**
     * this ControllerToInputAdapter registers its controller to keyboard mappings itself,
     * but more important is that it saves
     *
     */
    private class MyControllerToInputAdapter extends ControllerToInputAdapter {

        public MyControllerToInputAdapter(ControllerMappings mappings) {
            super(mappings);

            addButtonMapping(BUTTON_ROTATE_CLOCKWISE, Input.Keys.SPACE);
            addButtonMapping(BUTTON_ROTATE_COUNTERCLOCK, Input.Keys.CONTROL_LEFT);
            addButtonMapping(BUTTON_START, Input.Keys.ENTER);
            addButtonMapping(BUTTON_CANCEL, Input.Keys.ESCAPE);
            addAxisMapping(AXIS_HORIZONTAL, Input.Keys.LEFT, Input.Keys.RIGHT);
            addAxisMapping(AXIS_VERTICAL, Input.Keys.UP, Input.Keys.DOWN);
            addButtonMapping(BUTTON_HARDDROP, Input.Keys.CONTROL_RIGHT);
            addButtonMapping(BUTTON_HOLD, Input.Keys.H);
            addButtonMapping(BUTTON_FREEZE, Input.Keys.F);
        }

        @Override
        protected boolean sendKeyDownToTarget(int keycode, Controller inputSourceController) {
            synchronized (this) {
                controllerInUse = inputSourceController;
                boolean handled = super.sendKeyDownToTarget(keycode, inputSourceController);
                controllerInUse = null;
                return handled;
            }
        }

        @Override
        protected boolean sendKeyUpToTarget(int keycode, Controller inputSourceController) {
            synchronized (this) {
                controllerInUse = inputSourceController;
                boolean handled = super.sendKeyUpToTarget(keycode, inputSourceController);
                controllerInUse = null;
                return handled;
            }
        }
    }
}
