package com.tiecode.plugin.autocode;

import com.tiecode.develop.plugin.chinese.base.api.compiler.TiecodeCompilerAction;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProvider;

public class AutoCodeCompilerAction extends TiecodeCompilerAction {

    @Override
    public void onProvideAnnotation(AnnotationProvider provider) {
        provider.registerAnnotation(DataClassProcessor.DATA_CLASS, new DataClassProcessor());
        provider.registerAnnotation(HashObjectProcessor.HASH_OBJECT, new HashObjectProcessor());
        provider.registerAnnotation(SerializeClassProcessor.SERIALIZE_CLASS, new SerializeClassProcessor());
        provider.registerAnnotation(ParameterNameProcessor.PARAM_NAME, new ParameterNameProcessor());
        provider.registerAnnotation(GenerateCreateProcessor.GENERATE_CREATE, new GenerateCreateProcessor());
        provider.registerAnnotation(SingleInstanceProcessor.SINGLE, new SingleInstanceProcessor());
        provider.registerAnnotation(ToMainPkgProcessor.TO_MAIN_PKG, new ToMainPkgProcessor());
        provider.registerAnnotation(CustomComponentProcessor.CUSTOM_COMPONENT, new CustomComponentProcessor());
        provider.registerAnnotation(CleanUpProcessor.CLEAN_UP, new CleanUpProcessor());
    }
}
