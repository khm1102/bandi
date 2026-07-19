package kr.ac.tukorea.bandi.domain.member.client.tukorea;

@FunctionalInterface
interface InitializationVectorGenerator {

    byte[] generate();
}
