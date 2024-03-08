package com.elijahwaswa.basedomains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppProfile {
    DEV("dev"),PROD("prod");
    private String value;
}
