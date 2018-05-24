package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerManageFocus;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ITouchActionButton;
import de.golfgl.lightblocks.menu.MusicButtonListener;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 13.01.2018.
 */

public class GlowLabelButton extends Button implements ITouchActionButton, MusicButtonListener.IMusicButton,
        IControllerManageFocus {
    public static final float SMALL_SCALE_MENU = .9f;
    public static final float FONT_SCALE_MENU = .6f;
    public static final float FONT_SCALE_SUBMENU = .45f;
    // quick hack if automatic determination of next actor to south does not work
    // often the case with GlowLabelButton because of its big padding
    public Actor focusToSouth;
    private final float smallScaleFactor;
    private final GlowLabel labelGroup;
    private final Color disabledFontColor;
    protected Label faLabel;
    private boolean highlighted;
    private boolean isFirstAct;
    private boolean colorTransition;
    private ScaleToAction scaleAction;
    private Action colorAction;
    private Color fontColor;
    private Cell faCell;

    public GlowLabelButton(String text, Skin skin, float fontScale, float smallScaleFactor) {
        this("", text, skin, fontScale, smallScaleFactor);
    }

    public GlowLabelButton(String faText, String text, Skin skin) {
        this(faText, text, skin, FONT_SCALE_MENU);
    }

    public GlowLabelButton(String faText, String text, Skin skin, float fontScale) {
        this(faText, text, skin, fontScale, SMALL_SCALE_MENU);
    }

    public GlowLabelButton(String faText, String text, Skin skin, float fontScale, float smallScaleFactor) {
        super();
        setSkin(skin);
        setStyle(new ButtonStyle());

        disabledFontColor = LightBlocksGame.COLOR_DISABLED;
        this.smallScaleFactor = smallScaleFactor;

        labelGroup = new GlowLabel(text, skin, fontScale) {
            @Override
            public float getPrefHeight() {
                return super.getPrefHeight() / getScaleY();
            }
        };

        if (faText != null && faText.length() > 0) {
            faLabel = new Label(faText, skin, FontAwesome.SKIN_FONT_FA);
            faCell = add(faLabel).padRight(5).padLeft(5).width(faLabel.getPrefWidth())
                    .height(faLabel.getPrefHeight()).expand();
            setFaLabelAlignment();
            labelGroup.setAlignment(Align.left);
        }
        add(labelGroup).fill();
        setLabelExpansion();

        highlighted = true;
        isFirstAct = true;
    }

    @Override
    public float getMinWidth() {
        // der MinWidth des Buttons wird so erhöht, dass er in jeder Skalierung passt. Gleichzeitig ist die einzelne
        // Zelle aber nicht vergrößert, damit die mittige Ausrichtung von mehreren Zellen gewahrt ist
        return super.getMinWidth() - (labelGroup.getPrefWidth() + labelGroup.getPrefWidth() / labelGroup.getScaleX());
    }

    private void setFaLabelAlignment() {
        if (faLabel != null) {
            faLabel.setAlignment(labelGroup.getText().length() > 0 ? Align.right : Align.center);
            if (labelGroup.getText().length() > 0)
                faCell.right();
            else
                faCell.center();
        }
    }

    public boolean isColorTransition() {
        return colorTransition;
    }

    public void setColorTransition(boolean colorTransition) {
        this.colorTransition = colorTransition;
    }

    @Override
    public void act(float delta) {
        if (isPressed() && colorAction != null)
            labelGroup.removeAction(colorAction);

        super.act(delta);

        boolean activated = isOver();

        if (activated && !highlighted) {
            labelGroup.setGlowing(true, isFirstAct);
            if (smallScaleFactor < 1f) {
                labelGroup.removeAction(scaleAction);
                scaleAction = Actions.scaleTo(1f, 1f, GlowLabel.GLOW_IN_DURATION / 2, Interpolation.circle);
                labelGroup.addAction(scaleAction);
            }
            highlighted = true;
        } else if (!activated && highlighted || isFirstAct) {
            labelGroup.setGlowing(false, isFirstAct);
            if (smallScaleFactor < 1f) {
                labelGroup.removeAction(scaleAction);
                scaleAction = Actions.scaleTo(smallScaleFactor, smallScaleFactor, isFirstAct ? 0 :
                        GlowLabel.GLOW_OUT_DURATION / 2, Interpolation.circleIn);
                labelGroup.addAction(scaleAction);
            }
            highlighted = false;
        }

        Color fontColor;
        fontColor = getActiveColor();

        if (fontColor != null && !fontColor.equals(this.fontColor)) {
            this.fontColor = new Color(fontColor);
            if (colorTransition) {
                labelGroup.removeAction(colorAction);
                colorAction = Actions.color(this.fontColor,
                        getColor() != fontColor ? GlowLabel.GLOW_IN_DURATION : GlowLabel.GLOW_OUT_DURATION);
                labelGroup.addAction(colorAction);
            } else
                labelGroup.setColor(this.fontColor);
        }

        if (faLabel != null) {
            faLabel.setColor(labelGroup.getColor());
            // Wird in setFontScale nicht überprüft, und ein unnötiges Setzen löst auf Tabellen stetiges layout() aus
            if (faLabel.getFontScaleX() != labelGroup.getScaleX())
                faLabel.setFontScale(labelGroup.getScaleX());
        }

        isFirstAct = false;
    }

    protected Color getActiveColor() {
        Color fontColor;
        if (isDisabled() && disabledFontColor != null)
            fontColor = disabledFontColor;
        else if (isPressed())
            fontColor = LightBlocksGame.EMPHASIZE_COLOR;
            //else if (isChecked && style.checkedFontColor != null)
            //    fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style
            // .checkedFontColor;
            //else if (isOver() && style.overFontColor != null)
            //    fontColor = style.overFontColor;
        else
            fontColor = getColor();
        return fontColor;
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        // RoundedTextButton hat Copy
        if (isDisabled && colorAction != null)
            labelGroup.removeAction(colorAction);

        super.setDisabled(isDisabled);
    }

    @Override
    public void touchAction() {
        // RoundedTextButton hat Copy
        if (!isPressed() && !isDisabled()) {
            labelGroup.removeAction(colorAction);
            colorAction = MyActions.getTouchAction(getTouchColor(), getActiveColor());
            labelGroup.addAction(colorAction);
        }
    }

    protected Color getTouchColor() {
        return LightBlocksGame.COLOR_FOCUSSED_ACTOR;
    }

    @Override
    public boolean isOver() {
        return super.isOver() || getStage() != null && getStage() instanceof ControllerMenuStage &&
                ((ControllerMenuStage) getStage()).getFocusedActor() == this;
    }

    @Override
    public void pack() {
        labelGroup.pack();
        if (faLabel != null)
            faLabel.pack();
        super.pack();
    }

    public void setText(String text) {
        labelGroup.setText(text);
        setLabelExpansion();
        setFaLabelAlignment();
    }

    protected void setLabelExpansion() {
        getCell(labelGroup).expand(labelGroup.getText().length() > 0, true);
    }

    public void setFaText(String text) {
        faLabel.setText(text);
        faLabel.setFontScale(1);
        faLabel.invalidate();
        faCell.width(faLabel.getPrefWidth()).height(faLabel.getPrefHeight());
        faLabel.setFontScale(labelGroup.getScaleX());
    }

    @Override
    public Actor getNextFocusableActor(ControllerMenuStage.MoveFocusDirection direction) {
        if (direction == ControllerMenuStage.MoveFocusDirection.south)
            return focusToSouth;

        return null;
    }

    @Override
    public Actor getNextFocusableActor(boolean next) {
        return null;
    }
}
