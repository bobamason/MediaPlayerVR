package org.masonapps.libgdxgooglevr.vr;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.proto.nano.Preferences;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 6/8/2017.
 * based on GvrArmModel script in the Gvr Unity SDK
 */

public class ArmModel {
    /// The Downward tilt or pitch of the laser pointer relative to the controller (degrees).
    public static final Quaternion pointerTilt = new Quaternion(Vector3.X, 15f);
    private static final Vector3 NEG_Z = new Vector3(0, 0, -1);
    /// Initial relative location of the shoulder (meters).
    private static final Vector3 DEFAULT_SHOULDER_RIGHT = new Vector3(0.19f, -0.19f, -0.03f);

    /// The range of movement from the elbow position due to accelerometer (meters).
    private static final Vector3 ELBOW_MIN_RANGE = new Vector3(-0.05f, -0.1f, 0.0f);
    private static final Vector3 ELBOW_MAX_RANGE = new Vector3(0.05f, 0.1f, 0.2f);

    /// Offset of the laser pointer origin relative to the wrist (meters)
    private static final Vector3 POINTER_OFFSET = new Vector3(0.0f, -0.009f, 0.099f);

    /// Rest position parameters for arm model (meters).
    private static final Vector3 ELBOW_POSITION = new Vector3(0.195f, -0.5f, -0.075f);
    private static final Vector3 WRIST_POSITION = new Vector3(0.0f, 0.0f, 0.25f);
    private static final Vector3 ARM_EXTENSION_OFFSET = new Vector3(-0.13f, 0.14f, 0.08f);

    /// Strength of the acceleration filter (unitless).
    private static final float GRAVITY_CALIB_STRENGTH = 0.999f;

    /// Strength of the velocity suppression (unitless).
    private static final float VELOCITY_FILTER_SUPPRESS = 0.99f;

    /// Strength of the velocity suppression during low acceleration (unitless).
    private static final float LOW_ACCEL_VELOCITY_SUPPRESS = 0.9f;

    /// Strength of the acceleration suppression during low velocity (unitless).
    private static final float LOW_VELOCITY_ACCEL_SUPPRESS = 0.5f;

    /// The minimum allowable accelerometer reading before zeroing (m/s^2).
    private static final float MIN_ACCEL = 1.0f;

    /// The expected force of gravity (m/s^2).
    private static final float GRAVITY_FORCE = 9.807f;

    /// Amount of normalized alpha transparency to change per second.
    private static final float DELTA_ALPHA = 4.0f;

    /// Angle ranges the for arm extension offset to start and end (degrees).
    private static final float MIN_EXTENSION_ANGLE = 7.0f;
    private static final float MAX_EXTENSION_ANGLE = 60.0f;

    /// Increases elbow bending as the controller moves up (unitless).
    private static final float EXTENSION_WEIGHT = 0.4f;
    private static ArmModel instance = null;
    private final DaydreamControllerInputListener daydreamControllerInputListener;
    /// Height of the elbow  (m).
//  [Range(0.0f, 0.2f)]
    public float addedElbowHeight = 0.0f;
    /// Depth of the elbow  (m).
//  [Range(0.0f, 0.2f)]
    public float addedElbowDepth = 0.0f;
    /// Controller distance from the face after which the controller disappears (meters).
//  [Range(0.0f, 0.4f)]
    public float fadeDistanceFromFace = 0.32f;
    /// Controller distance from face after which the tooltips appear (meters).
//  [Range(0.4f, 0.6f)]
    public float tooltipMinDistanceFromFace = 0.45f;
    /// When the angle (degrees) between the controller and the head is larger than
    /// this value, the tooltips disappear.
    /// If the value is 180, then the tooltips are always shown.
    /// If the value is 90, the tooltips are only shown when they are facing the camera.
//  [Range(0, 180)]
    public int tooltipMaxAngleFromCamera = 80;
    /// Determines if the shoulder should follow the gaze
    public GazeBehavior followGaze = GazeBehavior.DuringMotion;
    /// Vector to represent the pointer's location.
    /// NOTE: This is in meatspace coordinates.
    public Vector3 pointerPosition = new Vector3();
    /// Quaternion to represent the pointer's rotation.
    /// NOTE: This is in meatspace coordinates.
    public Quaternion pointerRotation = new Quaternion();
    /// Vector to represent the wrist's location.
    /// NOTE: This is in meatspace coordinates.
    public Vector3 wristPosition = new Vector3();
    /// Quaternion to represent the wrist's rotation.
    /// NOTE: This is in meatspace coordinates.
    public Quaternion wristRotation = new Quaternion();
    /// Vector to represent the elbow's location.
    /// NOTE: This is in meatspace coordinates.
    public Vector3 elbowPosition = new Vector3();
    /// Quaternion to represent the elbow's rotation.
    /// NOTE: This is in meatspace coordinates.
    public Quaternion elbowRotation = new Quaternion();
    /// Vector to represent the shoulder's location.
    /// NOTE: This is in meatspace coordinates.
    public Vector3 shoulderPosition = new Vector3();
    /// Vector to represent the shoulder's location.
    /// NOTE: This is in meatspace coordinates.
    public Quaternion shoulderRotation = new Quaternion();
    /// The suggested rendering alpha value of the controller.
    /// This is to prevent the controller from intersecting the face.
    /// The range is always 0 - 1 but can be scaled by individual
    /// objects when using the GvrBaseControllerVisual script.
    public float preferredAlpha = 1f;
    /// The suggested rendering alpha value of the controller tooltips.
    /// This is to only display the tooltips when the player is looking
    /// at the controller, and also to prevent the tooltips from intersecting the
    /// player's face.
    public float tooltipAlphaValue = 1f;
    public OnArmModelUpdateEventListener listener;
    /// Forward direction of the arm model.
    private Vector3 torsoDirection = new Vector3();
    /// Indicates if this is the first frame to receive new IMU measurements.
    private boolean firstUpdate;
    /// Multiplier for handedness such that 1 = Right, 0 = Center, -1 = left.
    private Vector3 handedMultiplier = new Vector3();
    private Controller controller;
    private Vector3 cameraForward = new Vector3();

    private ArmModel() {
        daydreamControllerInputListener = new DaydreamControllerInputListener() {
            @Override
            public void onConnectionStateChange(int connectionState) {

            }

            @Override
            public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {

            }

            @Override
            public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

            }
        };
        GdxVr.input.getDaydreamControllerHandler().addListener(daydreamControllerInputListener);

        UpdateHandedness();

        // Reset other relevant state.
        firstUpdate = true;
    }

    /// Use the GvrController singleton to obtain a singleton for this class.
    public static ArmModel newInstance() {
        if (instance == null) {
            instance = new ArmModel();
        }
        return instance;
    }

    void onDestroy() {
        // Unregister the controller update listener.
        GdxVr.input.getDaydreamControllerHandler().removeListener(daydreamControllerInputListener);

        // Reset the singleton instance.
        instance = null;
    }

    void updateHeadDirection(Vector3 forward) {
        cameraForward.set(forward);
    }

    private void OnControllerUpdate() {
//        if (GvrController.Recentered) {
//            ResetState();
//        }

        UpdateHandedness();
        UpdateTorsoDirection();
        ApplyArmModel();
//        UpdateTransparency();
        UpdatePointer();

        firstUpdate = false;
        if (listener != null) {
            listener.onArmModelUpdate(this);
        }
    }

    private void UpdateHandedness() {
        // Update user handedness if the setting has changed
        final int handedness = GdxVr.app.getGvrLayout().getGvrApi().getUserPrefs().getControllerHandedness();

        // Determine handedness multiplier.
        handedMultiplier.set(0, 1, 1);
        if (handedness == Preferences.UserPrefs.Handedness.RIGHT_HANDED) {
            handedMultiplier.x = 1.0f;
        } else if (handedness == Preferences.UserPrefs.Handedness.LEFT_HANDED) {
            handedMultiplier.x = -1.0f;
        }

        // Place the shoulder in anatomical positions based on the height and handedness.
        shoulderRotation.idt();
        shoulderPosition.set(DEFAULT_SHOULDER_RIGHT).scl(handedMultiplier);
    }

    private void UpdateTorsoDirection() {
        // Ignore updates here if requested.
        if (followGaze == GazeBehavior.Never) {
            return;
        }

        // Determine the gaze direction horizontally.
        Vector3 gazeDirection = new Vector3(cameraForward);
        gazeDirection.y = 0.0f;
        gazeDirection.nor();

        // Use the gaze direction to update the forward direction.
        if (followGaze == GazeBehavior.Always || firstUpdate) {
            torsoDirection = gazeDirection;
        } else if (followGaze == GazeBehavior.DuringMotion) {
//            float angularVelocity = controller..magnitude;
//            float gazeFilterStrength = MathUtils.clamp((angularVelocity - 0.2f) / 45.0f, 0.0f, 0.1f);
//            torsoDirection.slerp(gazeDirection, gazeFilterStrength);
        }

        // Rotate the fixed joints.
        shoulderRotation.setFromCross(NEG_Z, torsoDirection);
        shoulderPosition.mul(shoulderRotation);
    }

    private void ResetState() {
        // We've lost contact, quickly reset the state.
        firstUpdate = true;
    }

    private void ApplyArmModel() {
        // Find the controller's orientation relative to the player
        Quaternion controllerOrientation = new Quaternion(shoulderRotation);
        controllerOrientation.mul(controller.orientation.x, controller.orientation.y, controller.orientation.z, controller.orientation.w);

        // Get the relative positions of the joints
        elbowPosition.set(ELBOW_POSITION).add(0.0f, addedElbowHeight, addedElbowDepth);
        elbowPosition.scl(handedMultiplier);
        wristPosition.set(WRIST_POSITION).scl(handedMultiplier);
        Vector3 armExtensionOffset = new Vector3(ARM_EXTENSION_OFFSET).scl(handedMultiplier);

        // Extract just the x rotation angle
        Vector3 controllerForward = new Vector3(NEG_Z).mul(controllerOrientation);
        float xAngle = 90.0f - (float) Math.acos(Vector3.dot(controllerForward.x, controllerForward.y, controllerForward.z, 0, 1, 0));

        // Remove the z rotation from the controller
        Quaternion xyRotation = new Quaternion().setFromCross(NEG_Z, controllerForward);

        // Offset the elbow by the extension
        float normalizedAngle = (xAngle - MIN_EXTENSION_ANGLE) / (MAX_EXTENSION_ANGLE - MIN_EXTENSION_ANGLE);
        float extensionRatio = MathUtils.clamp(normalizedAngle, 0.0f, 1.0f);
        elbowPosition.add(armExtensionOffset.scl(extensionRatio));

        // Calculate the lerp interpolation factor
        float totalAngle = xyRotation.getAngle();
        float lerpSuppresion = 1.0f - (float) Math.pow(totalAngle / 180.0f, 6);
        float lerpValue = lerpSuppresion * (0.4f + 0.6f * extensionRatio * EXTENSION_WEIGHT);

        // Apply the absolute rotations to the joints
        Quaternion lerpRotation = new Quaternion().slerp(xyRotation, lerpValue).conjugate();
        elbowRotation.set(shoulderRotation).mul(lerpRotation).mul(controllerOrientation);
        wristRotation.set(shoulderRotation).mul(controllerOrientation);

        // Determine the relative positions
        elbowPosition.mul(shoulderRotation);
        wristPosition.set(wristPosition).mul(elbowRotation).add(elbowPosition);
    }

    private void UpdatePointer() {
        // Determine the direction of the ray.
        pointerPosition.set(POINTER_OFFSET).mul(wristRotation).add(wristPosition);
        pointerRotation.set(wristRotation).mul(pointerTilt);
    }

    /// Represents when gaze-following behavior should occur.
    public enum GazeBehavior {
        Never,        /// The shoulder will never follow the gaze.
        DuringMotion, /// The shoulder will follow the gaze during controller motion.
        Always        /// The shoulder will always follow the gaze.
    }

//    private void UpdateTransparency() {
//        // Determine how vertical the controller is pointing.
//        float animationDelta = DELTA_ALPHA * Time.deltaTime;
//        float distToFace = Vector3.Distance(wristPosition, Vector3.zero);
//        if (distToFace < fadeDistanceFromFace) {
//            preferredAlpha = Mathf.Max(0.0f, preferredAlpha - animationDelta);
//        } else {
//            preferredAlpha = Mathf.Min(1.0f, preferredAlpha + animationDelta);
//        }
//
//        float dot = Vector3.dot(wristRotation * Vector3.up, -wristPosition.normalized);
//        float minDot = (tooltipMaxAngleFromCamera - 90.0f) / -90.0f;
//        if (distToFace < fadeDistanceFromFace
//                || distToFace > tooltipMinDistanceFromFace
//                || dot < minDot) {
//            tooltipAlphaValue = Mathf.Max(0.0f, tooltipAlphaValue - animationDelta);
//        } else {
//            tooltipAlphaValue = Mathf.Min(1.0f, tooltipAlphaValue + animationDelta);
//        }
//    }

    /// Event handler that occurs when the state of the ArmModel is updated.
    public interface OnArmModelUpdateEventListener {
        void onArmModelUpdate(ArmModel armModel);
    }
}
