package com.kireiko.utils.math.client.impl;

import com.kireiko.utils.math.client.ClientMath;
import com.kireiko.utils.mcp.MathHelper;

public class VanillaMath implements ClientMath {
    @Override
    public float sin(float value) {
        return MathHelper.sin(value);
    }

    @Override
    public float cos(float value) {
        return MathHelper.cos(value);
    }

    public static float sqrt(float f) {
        return (float) Math.sqrt(f);
    }
}