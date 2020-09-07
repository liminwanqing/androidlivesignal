//
// Created by Administrator on 2020/9/4.
//

#ifndef SIGNALTEST_YYSTACK_H
#define SIGNALTEST_YYSTACK_H

#include <string>

class yyStack {
public:
    yyStack();
    virtual ~yyStack();
    static std::string getJavaTrace();
    static std::string getNativeTarce(int num);
};


#endif //SIGNALTEST_YYSTACK_H
