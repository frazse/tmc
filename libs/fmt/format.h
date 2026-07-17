#pragma once
#include <format>
#include <iostream>
#include <string_view>
#include <cstdio>

namespace fmt {
    using std::format;
    using std::format_context;
    using std::make_format_args;
    using std::vformat;

    template<typename... Args>
    void print(std::string_view fmt_str, Args&&... args) {
        std::cout << std::vformat(fmt_str, std::make_format_args(args...));
    }

    template<typename... Args>
    void print(std::FILE* f, std::string_view fmt_str, Args&&... args) {
        std::string s = std::vformat(fmt_str, std::make_format_args(args...));
        std::fprintf(f, "%s", s.c_str());
    }
}
