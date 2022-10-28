package com.tiecode.plugin.autocode;

import com.tiecode.develop.plugin.chinese.base.api.compiler.TiecodeCompilerAction;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProvider;

public class AutoCodeCompilerAction extends TiecodeCompilerAction {
    @Override
    public void onProvideAnnotation(AnnotationProvider provider) {
        provider.registerAnnotation(DataClassProcessor.DATA_CLASS, new DataClassProcessor());
        provider.registerAnnotation(HashObjectProcessor.HASH_OBJECT, new HashObjectProcessor());
    }
}