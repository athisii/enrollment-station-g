package com.cdac.enrollmentstation.controller;

import com.cdac.enrollmentstation.util.DisplayUtil;

/**
 * @author athisii, CDAC
 * Created on 07/08/23
 */
public abstract class AbstractBaseController {
    public abstract void onUncaughtException();

    public int getSmallImageSize() {
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_FHD[0]) {
            return 60;
        }
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_HD[0]) {
            return 50;
        }
        return 40;
    }

    public int getLargeImageSize() {
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_FHD[0]) {
            return 400;
        }
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_HD[0]) {
            return 300;
        }
        return 200;
    }

    public int getHBoxSpacing() {
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_FHD[0]) {
            return 250;
        }
        if (DisplayUtil.SCREEN_WIDTH >= DisplayUtil.SCREEN_HD[0]) {
            return 180;
        }
        return 120;
    }


}
