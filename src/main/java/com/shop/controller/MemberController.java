package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RequestMapping(value = "/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    //회원가입 페이지로 이동
    @GetMapping(value = "/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());

        return "member/memberForm";
    }

    //입력된 정보를 토대로 회원 등록
    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    //개인 정보 수정 페이지로 이동
    @GetMapping(value = "/user")
    public String userDtl(Principal principal, Model model){
        MemberFormDto memberFormDto = memberService.getMypageList(principal.getName());
        model.addAttribute("memberFormDto", memberFormDto);

        return "member/userForm";
    }

    //개인 정보 수정
    @PostMapping(value = "/user/{memberId}")
    public String memberUserUpdate(@Valid MemberFormDto memberFormDto, BindingResult bindingResult,
                                   Model model){
        if(bindingResult.hasErrors()){
            return "member/userForm";
        }
        try{

            memberService.updateMember(memberFormDto, passwordEncoder);
        }catch (Exception e){
            model.addAttribute("errorMessage","회원정보 수정 중 에러가 발생하였습니다.");
            return "member/userForm";
        }

        return "redirect:/";
    }

    //(관리자) 회원 정보 수정 페이지로 이동
    @GetMapping(value = "/{memberId}")
    public String memberDtl(@PathVariable("memberId") Long memberId, Model model){
        try{
            MemberFormDto memberFormDto = memberService.getMemberDtl(memberId);
            model.addAttribute("memberFormDto", memberFormDto);
        } catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 회원입니다.");
            model.addAttribute("memberFormDto", new MemberFormDto());
        }
        return "member/adminForm";
    }

    //(관리자) 회원 정보 수정
    @PostMapping(value = "/{memberId}")
    public String memberUpdate(@Valid MemberFormDto memberFormDto, BindingResult bindingResult,
                               Model model){
        if(bindingResult.hasErrors()){
            return "member/adminForm";
        }
        try{
            memberService.updateMember(memberFormDto, passwordEncoder);
        }catch (Exception e){
            model.addAttribute("errorMessage","회원정보 수정 중 에러가 발생하였습니다.");
            return "member/adminForm";
        }

        return "redirect:/";
    }

    //(관리자) 회원 목록 페이지로 이동
    @GetMapping(value = "/list")
    public String memberList(Model model){
        List<Member> memberList = memberService.findMembers();
        model.addAttribute("memberList",memberList);

        return "member/memberList";
    }

    //로그인 페이지로 이동
    @GetMapping(value = "/login")
    public String loginMember(){
        return "/member/memberLoginForm";
    }

    //로그인 정보가 잘못되었을 시 에러메시지 출력
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }

    //(관리자) 회원 삭제
    @GetMapping(value = "/delete/{membersId}")
    public String deleteMember(@PathVariable("membersId") Long memberId){
        memberService.deleteMember(memberId);
        return "redirect:/members/list";
    }

}