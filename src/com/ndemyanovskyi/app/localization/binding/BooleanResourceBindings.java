/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.res.Resources;

public class BooleanResourceBindings extends ToStringResourceBindings<Boolean> {

    BooleanResourceBindings() {
	super(l -> Resources.booleans(l));
    }

}
