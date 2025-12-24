package com.ticketstorm.service.composite.register;


import com.ticketstorm.dto.UserRegisterDto;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.initialize.impl.composite.AbstractComposite;



public abstract class AbstractUserRegisterCheckHandler extends AbstractComposite<UserRegisterDto> {
    
    @Override
    public String type() {
        return CompositeCheckType.USER_REGISTER_CHECK.getValue();
    }
}
