package mchorse.bbs.particles.components;

import mchorse.bbs.particles.emitter.Particle;
import mchorse.bbs.particles.emitter.ParticleEmitter;

public interface IComponentParticleInitialize extends IComponentBase {
    public void apply(ParticleEmitter emitter, Particle particle);
}