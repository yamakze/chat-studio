package com.wokoba.czh.api.group;

import jakarta.validation.GroupSequence;

public interface ValidatorGroups {

    @GroupSequence({ValidatorGroups.FirstValidationGroup.class, ValidatorGroups.SecondValidationGroup.class})
    interface ValidationOrder {
    }

    interface FirstValidationGroup {
    }

    interface SecondValidationGroup {
    }

    interface Create {
    }

    interface Update {
    }
}
