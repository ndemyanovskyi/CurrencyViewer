/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.util.Compare;
import com.ndemyanovskyi.util.Unmodifiable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class AnimatorGroup extends Animator {
    
    private final List<Animator> animators;
    private final Duration duration;
    private final Set<Node> nodes;
    
    public AnimatorGroup(Animator... animators) {
        this(Arrays.asList(animators));
    }
    
    public AnimatorGroup(Collection<? extends Animator> animators) {
        Objects.requireNonNull(animators, "animators");
        
        if(animators.isEmpty()) {
            throw new IllegalArgumentException("animators is empty.");
        }
        
        Set<Node> modifiableNodes = new HashSet<>();
        for(Animator a : animators) {
            modifiableNodes.addAll(a.getNodes());
        }
        
        this.duration = Compare.max(Animator::getDuration, animators);
        this.animators = Unmodifiable.list(new ArrayList<>(animators));
        this.nodes = Unmodifiable.set(modifiableNodes);
    }

    @Override
    public void playUp() {
        animators.forEach(Animator::playUp);
    }

    @Override
    public void playDown() {
        animators.forEach(Animator::playDown);
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public List<Animator> getAnimators() {
        return animators;
    }

    @Override
    public Set<Node> getNodes() {
        return nodes;
    }
    
}
