package com.theuran.test.bridge;

import com.theuran.test.TestEngine;

public class BaseBridge {
    protected TestEngine engine;

    public BaseBridge(TestEngine engine) {
        this.engine = engine;
    }
}