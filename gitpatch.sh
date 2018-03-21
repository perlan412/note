#!/bin/bash
#git log --pretty=format:"%h---%s" -20 >temp.txt
#----------------------------------------------------------------
#Filename:	gitpatch.sh
#Revision:	2.0
#Date:		2017-04-13
#Author:	weilan
#Note:		将gitpatch.sh 放在版本库的同级目录，先创建temp.txt里面填写要生成patch的节点（主要要连续的）
#			格式%h---%s 
#				哈希值---commit信息
#			可以使用git log --pretty=format:"%h---%s" -4 >temp.txt
#			执行脚本之后会生成../Allpatchforgit/目录，里面包含所有patch
#			注temp.txt中的添加的信息要比想要生成的多一条，以便获取最后一条old的文件夹
#			例如git log --pretty=format:"%h---%s" -4 >temp.txt 事实上就值生成3个patch
#V1.0
#第一个版本
#V1.1
#修改带有空格的文件不能正常缩
#V2.0
#添加传入哈希值直接生成patch
#----------------------------------------------------------------

function pacth(){
	sed -i 's/\"//g' temp.txt		#去“，samba无法正常显示
	src=Allpatchforgit/
	num=0
	echo  'grace_chenline' >>temp.txt
	sed -i 's/grace_chenline/\ /' temp.txt #只能读取到1行，只能添加在删除在 
	while read line; do		
		hashvalue[$num]=`echo $line|awk -F '---' '{print $1}'`	#每一行用---分割取第一个
		commitsting[$num]="`echo $line | sed  -e "s/\///" |awk -F '---' '{print $2}'`"	#每一行用---分割取第二个 ￥0为去全部
		let num++	#与i++等效
	done <temp.txt
	
	echo temp.txt文件行数:$num

	IFS=$(echo -en "\n\b")	#文件名有空的无法解压 更换分割符
	echo IFS
	echo $IFS
	echo -en $IFS

	for ((i=0;i<`expr $num - 1 `;i++)); do
		if [[ ${commitsting[$i]:0:7} == "change " || ${commitsting[$i]:0:2} == "驱动" ]];then	#节点中以change和驱动开头的不生成patch
			continue
		fi
		if [ "Z${hashvalue[$i]}" == "Z" -o "Z${commitsting[$i]}" == "Z" -o  "Z${hashvalue[`expr $i + 1`]}" == "Z" -o "Z${commitsting[$i]}" == "Z" ];then
			continue
		fi 
		echo ${hashvalue[$i]}---${commitsting[$i]}
		
		git archive ${hashvalue[$i]} --prefix=$src${commitsting[$i]// /}/new/     $(tmp=$(echo -e "$(git diff --name-status ${hashvalue[`expr $i + 1`]} ${hashvalue[$i]})" | awk '$1!="D" {print $2}');test -n "$tmp" && echo -e "$tmp" || echo "Makefile")  |tar -xf -
						#生成patch中new文件夹
		git archive ${hashvalue[`expr $i + 1`]} --prefix=$src${commitsting[$i]// /}/old/   $(tmp=$(echo -e "$(git diff --name-status ${hashvalue[$i]} ${hashvalue[`expr $i + 1`]})" | awk '$1!="D" {print $2}');test -n "$tmp" && echo -e "$tmp" || echo "Makefile") |tar -xf -		#生成patch中old文件夹

		git format-patch -1 ${hashvalue[$i]} --output-directory "$src${commitsting[$i]// /}/" --numbered-files	#用format-patch生成 命名为1

		mv "$src${commitsting[$i]// /}/1"  "$src${commitsting[$i]// /}/${commitsting[$i]// /}.patch"	#重命名

		#tar czvf $src${commitsting[$i]// /}.tar $src${commitsting[$i]// /}		#压缩
	done
	
	if [ ! -d "./../$src" ];then
		mkdir -p ./../$src
	fi
	mv -nf $src/* ./../$src/  2> /dev/null
	rm -rf $src	
	rm temp.txt
#while read line;
# do done <temp.txt
}
if [ -n "$1" -a "$1" == "-help" -o "$1" == "-Help" ];then

	echo -e "\033[32mgitpatch.sh :  
	./gitpatch.sh	                           不带任何参数, 生成最近1次提交的path 
	./gitpatch.sh -n 10                        生成最近指定次提交的patch，本例是生成最近10次提交
	./gitpatch.sh -h 55f1cb8f60                生成指定节点的patch
	./gitpatch.sh -H 55f1cb8f60 58038666afa4   生成指定的两个节点之间的所有提交的patch
	./gitpatch.sh -p kernel/drivers            生成包含指定路径'kernel/drivers'修改的最近1次提交的patch
	./gitpatch.sh -p kernel/drivers 10         生成包含指定路径修改的指定次提交的patch，本例是生成最近10次提交
			\033[0m"
	exit 0
fi


if [ -n "$1" ] ; then
	while getopts "npHh" Options
	do
		case $Options in
		  n)
		  	shift;
		  	git log --pretty=format:"%h---%s" -`expr $1 + 1 ` >temp.txt
		  ;;
		  p)
		  shift;
		  	if [ -n "$2" ];then
		  		git log  --pretty=format:"%h---%s" -`expr $2 + 1 ` $1 >temp.txt
		  	else
		  		git log  --pretty=format:"%h---%s" -2  $1 >temp.txt
		  	fi
		  ;;
		  H)
		   	shift;
		   	if [ "${#1}" -gt "7" ];then
		   		startIndex=`git log --pretty=format:"%h---%s" | nl | grep ${1:0:7}  | awk '{print $1}'`
		   	fi 
		   
		   	if [ "${#2}" -gt "7" ];then
		   		endIndex=`git log --pretty=format:"%h---%s" | nl | grep ${2:0:7} | awk '{print $1}'`
		   		
		   	fi 
		   	if [ "$startIndex" -gt "$endIndex" ];then
		   			git log $2 --pretty=format:"%h---%s" -`expr $startIndex - $endIndex + 2` >temp.txt
		   	else
		   			git log $1 --pretty=format:"%h---%s" -`expr $endIndex - $startIndex + 2` >temp.txt
		   	fi	
		   	
		  ;;
		  h)
		  shift;
		  	git log $1 --pretty=format:"%h---%s" -2 >temp.txt
		  ;;
		 esac
		
	done
	pacth
else
	git log --pretty=format:"%h---%s" -2 >temp.txt
	pacth
fi

exit 0
#读取temp.txt中的每一行
#expr 是一款表达式计算工具，使用它能完成表达式求值操作，注意空格

#ls -lrt Allpatchforgit/|grep ^d >menu.txt
