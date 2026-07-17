#include <format>
#include <string>
int main() {
    std::string s = std::format("hello {}", 42);
    return 0;
}
