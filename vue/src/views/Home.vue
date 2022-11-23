<template>

<!--  <div stype="margin: 10px 0">-->
<!--    B站所有的付费笔记、源码、代码生成器、成品项目等都打包在VIP群<br><br>-->
<!--      5.5 - 5.31 活动价永久VIP打6折仅需 66 元，机不可失，时不再来！<br><br>-->
<!--      如果需要可以加我钉钉：xiaqing1993，微信：xia_qing2012-->
<!--  </div>-->
<!--  -->
  <div style="padding: 10px">
    <el-row :gutter="10">
      <el-col :span="12">
        <el-card>
          用户量：{{summary.members}}
        </el-card>

        <el-card>
          <div id="myChart" :style="{width: '600px', height: '500px'}"></div>
        </el-card>

      </el-col>
      <el-col :span="12">
        <el-card>
          新闻量：{{summary.newsc}} || 评论量：{{summary.newsMessage}}
        </el-card>

        <!-- 新闻浏览排行 -->
        <el-card>
          <div id="myChart2" :style="{width: '600px', height: '500px'}"></div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Home",
  data() {
    return {
      summary:{},
    }
  },
  mounted() {
    this.drawLine();
    this.barGraph();
  },
  created() {
    this.load()
  },
  methods: {
    load(){
      request.get("/news/summary").then(res => {
        if (res.code === '0') {
          this.summary = res.data
        }
      })
    },
    drawLine() {
      // 基于准备好的dom，初始化echarts实例
      let myChart = this.$root.echarts.init(document.getElementById('myChart'))
      let option = {
        title: {
          text: '各地区用户比例统计图',
          subtext: '实时数据',
          left: 'left'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          trigger: 'item',
          left: 'center'
        },
        toolbox: {
          show: true,
          feature: {
            mark: {show: true},
            dataView: {show: true, readOnly: false},
            restore: {show: true},
            saveAsImage: {show: true}
          }
        },
        series: [
          {
            name: '用户比例',
            type: 'pie',
            radius: [50, 150],
            center: ['50%', '60%'],
            roseType: 'area',
            itemStyle: {
              borderRadius: 8
            },
            data: []
          }
        ]
      }
      request.get("/user/count").then(res => {
        if (res.code === '0') {
          res.data.forEach(item => {
            option.series[0].data.push({name: item.address, value: item.count})
          })
          // 绘制图表
          myChart.setOption(option);
        }
      })

    },
    //右边横向条形图
    barGraph() {
      let catedate = {}
      request.get("/news/summary2").then(res => {
        if (res.code === '0') {
          catedate = res.data

          //初始化图标
          var myChart = this.$root.echarts.init(document.getElementById('myChart2'));
          //Y轴的数据，和数据值位置一一对应
          var cate = catedate.news_arr;
          //数据值，顺序和Y轴的名字一一对应
          var barData = catedate.count_arr;
          var option = {

            title: {

              text:  "排行榜top5",
            },
            tooltip: {

              trigger: "axis",
              axisPointer: {

                type: "shadow",
              },
            },
            //图表位置
            grid: {

              left: "3%",
              right: "4%",
              bottom: "3%",
              containLabel: true,
            },
            //X轴
            xAxis: {

              type: "value",
              axisLine: {

                show: false,
              },
              axisTick: {

                show: false,
              },
              //不显示X轴刻度线和数字
              splitLine: {
                show: false },
              axisLabel: {
                show: false },
            },
            yAxis: {

              type: "category",
              data: cate,
              //升序
              inverse: true,
              splitLine: {
                show: false },
              axisLine: {

                show: false,
              },
              axisTick: {

                show: false,
              },
              //key和图间距
              offset: 10,
              //动画部分
              animationDuration: 300,
              animationDurationUpdate: 300,
              //key文字大小
              nameTextStyle: {

                fontSize: 5,
              },
            },
            series: [
              {

                //柱状图自动排序，排序自动让Y轴名字跟着数据动
                realtimeSort: true,
                name: "数量",
                type: "bar",
                data: barData,
                barWidth: 14,
                barGap: 10,
                smooth: true,
                valueAnimation: true,
                //Y轴数字显示部分
                label: {

                  normal: {

                    show: true,
                    position: "right",
                    valueAnimation: true,
                    offset: [5, -2],
                    textStyle: {

                      color: "#333",
                      fontSize: 13,
                    },
                  },
                },
                itemStyle: {

                  emphasis: {

                    barBorderRadius: 7,
                  },
                  //颜色样式部分
                  normal: {

                    barBorderRadius: 7
                  },
                },
              },
            ],
            //动画部分

            animationDuration: 0,
            animationDurationUpdate: 3000,
            animationEasing: "linear",
            animationEasingUpdate: "linear",
          };
          myChart.setOption(option);
          //图表大小变动从新渲染，动态自适应
          window.addEventListener("resize", function () {

            myChart.resize();
          });
        }
      })

    }
  }
}
</script>

<style scoped>

</style>
