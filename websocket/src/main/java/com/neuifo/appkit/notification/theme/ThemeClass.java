/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.neuifo.appkit.notification.theme;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.neuifo.appkit.R;
import com.neuifo.appkit.notification.model.NcViewWrapper;
import com.neuifo.appkit.notification.util.Mlog;
import com.neuifo.appkit.notification.util.RoundDrawable;
import java.util.Date;

/**
 * Extend this class and override any methods you need to modify.
 */
public class ThemeClass {

    public ThemeClass(ViewStub stub) {
        stub.setLayoutResource(R.layout.im_activity_read_inner);
    }

    public ThemeClass(NcViewWrapper ncViewWarrper, ViewStub stub) {
        stub.setLayoutResource(ncViewWarrper.getRootId());
    }

    public ThemeClass() {

    }

    /**
     * Called right after the theme has been inflated
     *
     * @param layout The root layout
     */
    public void init(LinearLayout layout) {

    }

    /**
     * Fetch the root view of the theme
     *
     * @param layout The root layout
     */
    public ViewGroup getRootView(LinearLayout layout, @IdRes int id) {
        if (id == 0) {
            id = R.id.im_linearLayout;
        }
        return (ViewGroup) layout.findViewById(id);
    }

    /**
     * Show the time the notification arrived
     *
     * @param layout The root layout
     * @param time   Date object containing the time the notification arrived
     */
    public void showTime(LinearLayout layout, @IdRes int timeId, Date time) {
        if (timeId == 0) {
            timeId = R.id.im_timeView;
        }
        final TextView timeView = (TextView) layout.findViewById(timeId);
        if (timeView != null) {
            timeView.setText(DateFormat.getTimeFormat(layout.getContext().getApplicationContext()).format(time));
            timeView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The the time view
     *
     * @param layout The root layout
     */
    public void hideTime(LinearLayout layout, @IdRes int timeId) {
        if (timeId == 0) {
            timeId = R.id.im_timeView;
        }
        View viewById = layout.findViewById(timeId);
        if (viewById != null) viewById.setVisibility(View.GONE);
    }

    /**
     * Fetch a reference to the action button area
     *
     * @param layout The root layout
     */
    public ViewGroup getActionButtons(LinearLayout layout,@IdRes int actionId) {
        if(actionId == 0){
            actionId = R.id.action_buttons;
        }
        return (ViewGroup) layout.findViewById(actionId);
    }

    /**
     * Remove all action buttons from the layout, in case the layout needs to be re-used.
     */
    public void removeActionButtons(ViewGroup actionButtonViewGroup) {
        while (actionButtonViewGroup.getChildCount() > 0) {
            actionButtonViewGroup.removeViewAt(0);
        }
    }

    /**
     * This notification does have action buttons. Display the action button area.
     *
     * @param layout The root layout
     */
    public void showActionButtons(LinearLayout layout, @IdRes int ids) {
        if(ids == 0){
            ids = R.id.im_button_container;
        }
        View viewById = layout.findViewById(ids);
        if(viewById!=null) viewById.setVisibility(View.VISIBLE);
    }

    /**
     * This notification doesn't have any action buttons. Hide the action button area.
     *
     * @param layout The root layout
     */
    public void hideActionButtons(LinearLayout layout,@IdRes int actionId) {
        if(actionId == 0){
            actionId = R.id.action_buttons;
        }
        View viewById = layout.findViewById(actionId);
        if(viewById!=null) viewById.setVisibility(View.GONE);
    }

    /**
     * Add an action button to the layout.
     */
    public void addActionButton(ViewGroup actionButtons, String actionTitle, Drawable icon, View.OnClickListener clickListener, float fontMultiplier) {
        LayoutInflater inflater = LayoutInflater.from(actionButtons.getContext());
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.im_button_notification, actionButtons);

        Button button = (Button) v.getChildAt(v.getChildCount() - 1);
        button.setText(actionTitle);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontMultiplier * button.getTextSize());
        if (icon != null) {
            icon.mutate().setColorFilter(getColorFilter(Color.BLACK));
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        button.setOnClickListener(clickListener);
    }

    /**
     * Return the view displaying the notification icon.
     */
    public ImageView getIconView(LinearLayout layout) {
        return (ImageView) layout.findViewById(R.id.im_notification_icon);
    }

    /**
     * Return the view displaying the small notification icon.
     * Should return null if the theme doesn't use small icons.
     */
    public ImageView getSmallIconView(LinearLayout layout) {
        return (ImageView) layout.findViewById(R.id.im_notification_icon_small);
    }

    /**
     * Set the notification icon from a bitmap.
     */
    public void setIcon(ImageView imageView, Bitmap bitmap, boolean round_icons, int color) {
        if (bitmap == null) return;
        if (round_icons) {
            final double minimumWidthForRoundIcon = imageView.getContext().getResources().
                    getDimension(R.dimen.notification_ic_size) / (2 * Math.cos(Math.toRadians(45)));
            int bitmapWidth = bitmap.getWidth();
            Mlog.v(bitmapWidth, minimumWidthForRoundIcon);

            if (bitmapWidth >= minimumWidthForRoundIcon) {
                try {
                    RoundDrawable roundedDrawable = new RoundDrawable(bitmap);
                    imageView.setImageDrawable(roundedDrawable);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } catch (Exception e) {
                    e.printStackTrace();
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                imageView.setImageBitmap(bitmap);
            }
            imageView.setBackgroundResource(R.drawable.im_circle_grey);
            setColor(imageView, color);
        } else {
            imageView.setImageBitmap(bitmap);
            setColor(imageView, color);
        }
    }

    /**
     * Set the small notification icon.
     */
    public void setSmallIcon(ImageView smallIcon, Drawable drawable, int color) {
        if (drawable != null) {
            smallIcon.setImageDrawable(drawable);
            setColor(smallIcon, color);
        } else {
            smallIcon.setVisibility(View.GONE);
        }
    }

    protected static void setColor(View view, int color) {
        if (color == 0) return;
        Drawable drawable = view.getBackground();
        if (drawable != null) {
            drawable = drawable.mutate();
            //drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            drawable.setColorFilter(getColorFilter(color));
            if (Build.VERSION.SDK_INT >= 16) view.setBackground(drawable);
            else view.setBackgroundDrawable(drawable);
        } else {
            view.setBackgroundColor(color);
        }
    }

    /**
     * Fetch the dismiss button.
     */
    public View getDismissButton(LinearLayout layout,@IdRes int buttonId) {
        if(buttonId == 0){
            buttonId = R.id.im_notification_dismiss;
        }
        return layout.findViewById(buttonId);
    }


    /**
     * Hide the dismiss button.
     */
    public void hideDismissButton(View dismissButton) {
        dismissButton.setVisibility(View.GONE);
    }

    /**
     * In case you need to do something when stopping. Called after the view is removed from the window manager.
     */
    public void destroy(LinearLayout layout) {
    }

    /**
     * Get a color filter for recoloring any solid drawable.
     * From http://stackoverflow.com/a/11171509
     *
     * @param color The color
     * @return A ColorMatrixColorFilter
     */
    protected static ColorFilter getColorFilter(int color) {
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;

        float[] matrix = {
                0, 0, 0, 0, red,
                0, 0, 0, 0, green,
                0, 0, 0, 0, blue,
                0, 0, 0, 1, 0};

        return new ColorMatrixColorFilter(matrix);
    }
}