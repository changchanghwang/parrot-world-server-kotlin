package changchanghwang.parrotworldserver.services.members.application

import changchanghwang.parrotworldserver.common.auth.application.AuthService
import changchanghwang.parrotworldserver.common.exceptions.BadRequest
import changchanghwang.parrotworldserver.services.members.domain.Member
import changchanghwang.parrotworldserver.services.members.domain.services.ValidateMemberService
import changchanghwang.parrotworldserver.services.members.dto.MemberDto
import changchanghwang.parrotworldserver.services.members.infrastructure.MemberRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val validateMemberService: ValidateMemberService,
    private val authService: AuthService,
) {
    @Transactional
    fun signUp(signUpRequest: MemberDto.SignUpRequest) {
        val member =
            Member.of(
                signUpRequest.email,
                signUpRequest.password,
                signUpRequest.nickName,
                validateMemberService,
            )

        memberRepository.save(member)
    }

    @Transactional
    fun signIn(signInRequest: MemberDto.SignInRequest): MemberDto.SignInResponse {
        val member =
            memberRepository.findByEmail(signInRequest.email)
                ?: throw BadRequest("Unknown user(${signInRequest.email}) tried to sign-in.", "이메일 또는 패스워드가 올바르지 않습니다.")

        member.validatePassword(signInRequest.password, validateMemberService)
        val accessToken = authService.generateAccessToken(member.id!!)
        val refreshToken = authService.generateRefreshToken(null)

        member.updateRefreshToken(refreshToken)
        memberRepository.save(member)

        return MemberDto.SignInResponse(accessToken, refreshToken)
    }
}
