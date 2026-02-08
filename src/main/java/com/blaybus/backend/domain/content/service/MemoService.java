package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Memo;
import com.blaybus.backend.domain.content.dto.request.MemoRequest;
import com.blaybus.backend.domain.content.dto.response.MemoResponse;
import com.blaybus.backend.domain.content.repository.MemoRepository;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    private final MemoRepository memoRepository;
    private final MatchingRepository matchingRepository;

    private final UserRepository userRepository;

    private void validateMentorAccess(Long mentorId, Long menteeId) {
        boolean ok = matchingRepository.existsByMentorIdAndMenteeId(mentorId, menteeId);
        if (!ok) {
            throw new IllegalArgumentException("담당 멘티가 아니어서 접근할 수 없습니다.");
        }
    }

    public MemoResponse.ListResult getMemos(Long mentorId, Long menteeId) {
        validateMentorAccess(mentorId, menteeId);
        List<Memo> memos = memoRepository.findByMentee_IdOrderByCreatedAtDesc(menteeId);
        return MemoResponse.ListResult.from(memos);
    }

    public MemoResponse.ListResult getMemosLimit(Long mentorId, Long menteeId, int limit) {
        validateMentorAccess(mentorId, menteeId);

        int safeLimit = Math.min(Math.max(limit, 1), 20);
        List<Memo> memos = memoRepository.findByMentee_IdOrderByCreatedAtDesc(
                menteeId,
                PageRequest.of(0, safeLimit)
        );
        return MemoResponse.ListResult.from(memos);
    }

    //메모 작성
    @Transactional
    public MemoResponse.Item createMemo(Long mentorId, Long menteeId, MemoRequest.Create req) {
        validateMentorAccess(mentorId, menteeId);

        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 멘토입니다."));

        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 멘티입니다."));

        Memo memo = Memo.create(mentor, mentee, req.getContent());
        Memo saved = memoRepository.save(memo);

        return MemoResponse.Item.from(saved);
    }


    //메모 수정
    @Transactional
    public MemoResponse.Item updateMemo(Long mentorId, Long memoId, MemoRequest.Update req) {

        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 존재하지 않습니다."));

        // 작성자(멘토) 본인 메모인지 확인
        if (!memo.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        memo.updateContent(req.getContent()); // 엔티티에 update 메서드 필요
        return MemoResponse.Item.from(memo);
    }


    //메모 삭제
    @Transactional
    public MemoResponse.DeleteResult deleteMemo(Long mentorId, Long memoId) {

        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 존재하지 않습니다."));

        // 작성자(멘토) 본인 메모인지 확인
        if (!memo.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        memoRepository.delete(memo);
        return MemoResponse.DeleteResult.of(memoId);
    }


}
