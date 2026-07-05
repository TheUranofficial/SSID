package mchorse.bbs.ui.framework.elements.input.keyframes.generic.factories;

import mchorse.bbs.BBSSettings;
import mchorse.bbs.forms.properties.AnchorProperty;
import mchorse.bbs.l10n.keys.IKey;
import mchorse.bbs.ui.UIKeys;
import mchorse.bbs.ui.framework.elements.buttons.UIButton;
import mchorse.bbs.ui.framework.elements.input.keyframes.generic.UIPropertyEditor;
import mchorse.bbs.ui.utils.icons.Icons;
import mchorse.bbs.utils.colors.Colors;
import mchorse.bbs.utils.keyframes.generic.GenericKeyframe;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIAnchorKeyframeFactory extends UIKeyframeFactory<AnchorProperty.Anchor> {
    private UIButton actor;
    private UIButton attachment;

    public UIAnchorKeyframeFactory(GenericKeyframe<AnchorProperty.Anchor> keyframe, UIPropertyEditor editor) {
        super(keyframe, editor);

        this.actor = new UIButton(UIKeys.GENERIC_KEYFRAMES_ANCHOR_PICK_ACTOR, (b) -> this.displayActors());
        this.attachment = new UIButton(UIKeys.GENERIC_KEYFRAMES_ANCHOR_PICK_ATTACHMENT, (b) -> this.displayAttachments());

        this.add(this.actor, this.attachment);
    }

    private void displayActors() {
        this.getContext().replaceContextMenu((menu) ->
        {
            menu.action(Icons.CLOSE, UIKeys.GENERAL_NONE, Colors.NEGATIVE, () -> this.setActor(-1));
        });
    }

    private void displayAttachments() {
        Map<String, Matrix4f> map = new HashMap<>();

        List<String> attachments = new ArrayList<>(map.keySet());

        attachments.sort(String::compareToIgnoreCase);

        if (attachments.isEmpty()) {
            return;
        }

        String value = this.keyframe.getValue().attachment;

        this.getContext().replaceContextMenu((menu) ->
        {
            for (String attachment : attachments) {
                if (attachment.equals(value)) {
                    menu.action(Icons.LIMB, IKey.raw(attachment), BBSSettings.primaryColor(0), () -> this.setAttachment(attachment));
                } else {
                    menu.action(Icons.LIMB, IKey.raw(attachment), () -> this.setAttachment(attachment));
                }
            }
        });
    }

    private void setActor(int actor) {
        this.keyframe.getValue().actor = actor;

        this.editor.setValue(this.keyframe.getValue());
    }

    private void setAttachment(String attachment) {
        this.keyframe.getValue().attachment = attachment;

        this.editor.setValue(this.keyframe.getValue());
    }
}