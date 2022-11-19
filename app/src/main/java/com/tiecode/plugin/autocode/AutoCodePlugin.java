package com.tiecode.plugin.autocode;

import android.content.Context;

import com.tiecode.develop.plugin.chinese.base.api.compiler.TiecodeCompilerAction;
import com.tiecode.plugin.action.ActionController;
import com.tiecode.plugin.app.PluginApp;
import com.tiecode.plugin.autocode.action.AutoCodeCompilerAction;

public class AutoCodePlugin extends PluginApp {

    @Override
    public void onInitPlugin(Context context) {
        ActionController controller = new ActionController();
        controller.addAction(TiecodeCompilerAction.class, new AutoCodeCompilerAction());
        setActionController(controller);
    }
}
