package io.punchtime.punchtime.ui.behaviors;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.List;

/**
 * Created by elias on 24/04/16.
 * for project: Punchtime
 */
public class AvoidSnackbarBehavior extends CoordinatorLayout.Behavior<View> {
        // We only support the child <> Snackbar shift movement on Honeycomb and above. This is
        // because we can use view translation properties which greatly simplifies the code.
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;

        private ValueAnimator mchildTranslationYAnimator;
        private float mCardTranslationY;

        public AvoidSnackbarBehavior(Context context, AttributeSet attrs) {
            super(context,attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       View child, View dependency) {
            // We're dependent on all SnackbarLayouts (if enabled)
            return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                              View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updatechildTranslationForSnackbar(parent, child, dependency);
            }
            return false;
        }

        private void updatechildTranslationForSnackbar(CoordinatorLayout parent,
                                                     final View child, View snackbar) {
            final float targetTransY = getchildTranslationYForSnackbar(parent, child);
            if (mCardTranslationY == targetTransY) {
                // We're already at (or currently animating to) the target value, return...
                return;
            }

            final float currentTransY = ViewCompat.getTranslationY(child);

            // Make sure that any current animation is cancelled
            if (mchildTranslationYAnimator != null && mchildTranslationYAnimator.isRunning()) {
                mchildTranslationYAnimator.cancel();
            }

            if (child.isShown()
                    && Math.abs(currentTransY - targetTransY) > (child.getHeight() * 0.667f)) {
                // If the child will be travelling by more than 2/3 of it's height, let's animate
                // it instead
                if (mchildTranslationYAnimator == null) {
                    mchildTranslationYAnimator = new ValueAnimator();
                    mchildTranslationYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mchildTranslationYAnimator.addUpdateListener(
                            new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    ViewCompat.setTranslationY(child,
                                            animator.getAnimatedFraction());
                                }
                            });
                }
                mchildTranslationYAnimator.setFloatValues(currentTransY, targetTransY);
                mchildTranslationYAnimator.start();
            } else {
                // Now update the translation Y
                ViewCompat.setTranslationY(child, targetTransY);
            }

            mCardTranslationY = targetTransY;
        }

        private float getchildTranslationYForSnackbar(CoordinatorLayout parent,
                                                         View child) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                final View view = dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                    minOffset = Math.min(minOffset,
                            ViewCompat.getTranslationY(view) - view.getHeight());
                }
            }

            return minOffset;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, View child,
                                     int layoutDirection) {
            // Now let the CoordinatorLayout lay out the child
            parent.onLayoutChild(child, layoutDirection);
            // Now offset it if needed
            offsetIfNeeded(parent, child);
            return true;
        }

        /**
         * Pre-Lollipop we use padding so that the shadow has enough space to be drawn. This method
         * offsets our layout position so that we're positioned correctly if we're on one of
         * our parent's edges.
         */
        private void offsetIfNeeded(CoordinatorLayout parent, View child) {
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();

            int offsetTB = 0, offsetLR = 0;

            if (child.getRight() >= parent.getWidth() - lp.rightMargin) {
                // If we're on the left edge, shift it the right
                offsetLR = child.getPaddingRight();
            } else if (child.getLeft() <= lp.leftMargin) {
                // If we're on the left edge, shift it the left
                offsetLR = -child.getPaddingLeft();
            }
            if (child.getBottom() >= parent.getBottom() - lp.bottomMargin) {
                // If we're on the bottom edge, shift it down
                offsetTB = child.getPaddingBottom();
            } else if (child.getTop() <= lp.topMargin) {
                // If we're on the top edge, shift it up
                offsetTB = - child.getPaddingTop();
            }

            child.offsetTopAndBottom(offsetTB);
            child.offsetLeftAndRight(offsetLR);
        }
    }