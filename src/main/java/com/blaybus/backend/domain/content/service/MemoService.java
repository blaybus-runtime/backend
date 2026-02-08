package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Memo;
import com.blaybus.backend.domain.content.dto.response.MemoResponse;
import com.blaybus.backend.domain.content.repository.MemoRepository;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
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
}
