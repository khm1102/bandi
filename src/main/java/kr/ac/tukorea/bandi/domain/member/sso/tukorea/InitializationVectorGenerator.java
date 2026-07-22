package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

@FunctionalInterface
interface InitializationVectorGenerator {

    byte[] generate();
}
