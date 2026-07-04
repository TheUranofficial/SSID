package mchorse.bbs.particles.components;

import mchorse.bbs.particles.emitter.Particle;
import mchorse.bbs.particles.emitter.ParticleEmitter;

public interface IComponentParticleUpdate extends IComponentBase {
    public void update(ParticleEmitter emitter, Particle particle);
}