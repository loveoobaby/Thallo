import React, { PureComponent } from 'react';
import * as echarts from 'echarts';
import 'zrender/lib/svg/svg';
import moment from 'moment';
import PropTypes from 'prop-types';

export default class LineChart extends PureComponent {
  constructor(props) {
    super(props);
    this.state = { optionData: this.props.optionData, id: this.props.id };
  }

  componentDidMount() {
    this.setOption(this.props.optionData);
  }

  componentDidUpdate() {
    this.setOption(this.props.optionData);
  }

  componentWillUnmount() {
    this.dispose();
  }


  render() {
    return (
      <div id={this.state.id} style={{ width: '100%', height: 300 }}>
        {this.state.id}
      </div>
    );
  }

  setOption = optionData => {
    let maxValue = 0;
    for (let i = 0; i < optionData.length; i++) {
      let aa = optionData[i];
      if (aa.value[1] > maxValue) {
        maxValue = aa.value[1];
      }
    }

    let scale = 1;
    while (maxValue / scale > 900) {
      scale = scale * 10;
    }

    let yName = '';
    if (scale > 1) {
      yName = '×' + scale;
    }

    let minInterval = 0.1;
    if (maxValue === 1) {
      minInterval = 0.1;
    } else {
      minInterval = 1;
    }

    const myChart = echarts.init(document.getElementById(this.state.id));
    const optionChoice = {
      tooltip: {
        trigger: 'axis',
        formatter(params) {
          params = params[0];
          if (params.value[1] !== null)
            return `${moment(parseInt(params.value[0])).format(
              'YYYY/MM/DD HH:mm:ss'
            )}<br />${params.value[1].toFixed(2)}`;
          return '';
        },
        axisPointer: {
          animation: false,
        },
      },

      xAxis: {
        type: 'time',
        splitLine: {
          show: false,
          lineStyle: {
            color: ['green', 'green'],
            width: 0.5,
          },
        },
        // splitNumber: 10,
        axisLabel: {
          show: true,
          formatter: function(value, index) {
            const date = new Date(value);
            let minute = date.getMinutes();
            if (minute < 10) {
              minute = '0' + minute;
            }
            const texts = [date.getHours(), minute];
            return texts.join(':');
          },
        },
      },
      yAxis: {
        type: 'value',
        // max: Math.round(maxValue),
        name: yName,
        splitLine: {
          show: true,
          lineStyle: {
            color: ['#e8e8e8', '#e8e8e8'],
            width: 1.5,
            type: 'dashed',
          },
        },
        axisPointer: {
          z: 1000,
        },

        nameTextStyle: {
          fontSize: 15,
        },

        axisTick: {
          show: false,
        },
        // name: yName,
        minInterval: minInterval,
        axisLabel: {
          formatter: function(value, index) {
            return value / scale;
          },
        },
      },
      series: [
        {
          type: 'line',
          lineStyle: {
            normal: {
              width: 0.5,
            },
          },
          itemStyle: {
            color: this.props.lineColor,
          },
          showSymbol: false,
          hoverAnimation: false,
          data: optionData,
          areaStyle: {
            color: this.props.areaColor,
          },
        },
      ],
    };
    myChart.setOption(optionChoice);
  };
  dispose = () => {
    if (!this.chart) {
      return;
    }

    this.chart.dispose();
    this.chart = null;
  };

  resize = () => {
    this.chart && this.chart.resize();
  };

}



LineChart.propTypes = {
  lineColor: PropTypes.string.isRequired,
  areaColor: PropTypes.string.isRequired,
};
